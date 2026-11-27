package com.chineselearning.chatbotservice.service;

import com.chineselearning.chatbotservice.client.GeminiClient;
import com.chineselearning.chatbotservice.client.GeminiClient.Turn;
import com.chineselearning.chatbotservice.config.ChatbotProperties;
import com.chineselearning.chatbotservice.dto.ChatMessageDto;
import com.chineselearning.chatbotservice.dto.ChatSessionDto;
import com.chineselearning.chatbotservice.dto.CreateSessionRequest;
import com.chineselearning.chatbotservice.dto.PagedResponse;
import com.chineselearning.chatbotservice.dto.SendMessageResponse;
import com.chineselearning.chatbotservice.entity.ChatMessage;
import com.chineselearning.chatbotservice.entity.ChatSession;
import com.chineselearning.chatbotservice.entity.Sender;
import com.chineselearning.chatbotservice.exception.SessionAccessDeniedException;
import com.chineselearning.chatbotservice.exception.SessionClosedException;
import com.chineselearning.chatbotservice.exception.SessionNotFoundException;
import com.chineselearning.chatbotservice.repository.ChatMessageRepository;
import com.chineselearning.chatbotservice.repository.ChatSessionRepository;
import com.chineselearning.chatbotservice.repository.SessionMessageStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final int AUTO_TITLE_LENGTH = 40;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final GeminiClient geminiClient;
    private final PromptFactory promptFactory;
    private final SendRateLimiter rateLimiter;
    private final TransactionTemplate readTx;
    private final TransactionTemplate writeTx;
    private final int contextWindowSize;

    public ChatService(ChatSessionRepository sessionRepository,
                       ChatMessageRepository messageRepository,
                       GeminiClient geminiClient,
                       PromptFactory promptFactory,
                       SendRateLimiter rateLimiter,
                       PlatformTransactionManager transactionManager,
                       ChatbotProperties properties) {

        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.geminiClient = geminiClient;
        this.promptFactory = promptFactory;
        this.rateLimiter = rateLimiter;
        this.readTx = new TransactionTemplate(transactionManager);
        this.readTx.setReadOnly(true);
        this.writeTx = new TransactionTemplate(transactionManager);
        this.contextWindowSize = properties.context().windowSize();
    }


    // ---------- sessions ----------

    @Transactional
    public ChatSessionDto createSession(Long studentId, CreateSessionRequest request)
    {
        String title = request.title() == null || request.title().isBlank() ? null : request.title().strip();
        String instructions = request.customInstructions() == null || request.customInstructions().isBlank()
                ? null
                : request.customInstructions().strip();
        ChatSession saved = sessionRepository.save(ChatSession.start(studentId, title, instructions));
        return ChatSessionDto.from(saved, null, 0);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDto> getSessions(Long studentId)
    {
        return toSessionDtos(sessionRepository.findByStudentIdOrderByStartedAtDescIdDesc(studentId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChatSessionDto> getSessionsPaged(Long studentId, int page, int size)
    {
        Page<ChatSession> sessions = sessionRepository
                .findByStudentIdOrderByStartedAtDescIdDesc(studentId, PageRequest.of(page, size));

        List<ChatSessionDto> dtos = toSessionDtos(sessions.getContent());
        return new PagedResponse<>(dtos, sessions.getNumber(), sessions.getSize(),
                sessions.getTotalElements(), sessions.getTotalPages(), sessions.isLast());
    }

    @Transactional
    public ChatSessionDto endSession(Long sessionId, Long studentId)
    {
        ChatSession session = loadOwned(sessionId, studentId);
        session.end(); // dirty checking persists it
        return toSessionDtos(List.of(session)).get(0);
    }

    @Transactional
    public ChatSessionDto renameSession(Long sessionId, Long studentId, String newTitle)
    {
        ChatSession session = loadOwned(sessionId, studentId);
        session.rename(newTitle.strip());
        return toSessionDtos(List.of(session)).get(0);
    }

    @Transactional
    public void deleteSession(Long sessionId, Long studentId)
    {
        ChatSession session = loadOwned(sessionId, studentId);
        messageRepository.deleteAllBySessionId(session.getId());
        sessionRepository.delete(session);
    }



    // ---------- messages ----------

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(Long sessionId, Long studentId)
    {
        loadOwned(sessionId, studentId);
        return messageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(sessionId).stream()
                .map(ChatMessageDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChatMessageDto> getMessagesPaged(Long sessionId, Long studentId, int page, int size)
    {
        loadOwned(sessionId, studentId);
        Page<ChatMessage> messages = messageRepository
                .findBySessionIdOrderByCreatedAtAscIdAsc(sessionId, PageRequest.of(page, size));
        return PagedResponse.from(messages, ChatMessageDto::from);
    }




    public SendMessageResponse sendMessage(Long sessionId, Long studentId, String content)
    {
        rateLimiter.check(studentId);

        // 1) short read-only transaction: validate + build the context
        SendContext context = readTx.execute(status -> {
            ChatSession session = loadOwned(sessionId, studentId);

            if (session.isEnded())
            {
                throw new SessionClosedException();
            }
            return new SendContext(session.getCustomInstructions(), loadContextTurns(sessionId));
        });

        // 2) external call, no transaction, no connection held
        List<Turn> turns = new ArrayList<>(context.history());
        turns.add(new Turn("user", content));
        String answer = geminiClient.generate(promptFactory.build(context.customInstructions()), turns);

        // 3) short write transaction
        return writeTx.execute(status -> {
            ChatSession session = loadOwned(sessionId, studentId);
            if (session.isEnded())
            {
                throw new SessionClosedException();
            }

            ChatMessage userMessage = messageRepository.save(ChatMessage.of(session, Sender.STUDENT, content));
            ChatMessage aiMessage = messageRepository.save(ChatMessage.of(session, Sender.AI, answer));

            if (!session.hasTitle())
            {
                session.rename(autoTitle(content));
            }
            return new SendMessageResponse(ChatMessageDto.from(userMessage), ChatMessageDto.from(aiMessage));
        });
    }



    // ---------- helpers ----------

    private record SendContext(String customInstructions, List<Turn> history) {
    }

    private ChatSession loadOwned(Long sessionId, Long studentId)
    {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.isOwnedBy(studentId))
        {
            throw new SessionAccessDeniedException();
        }
        return session;
    }

    private List<Turn> loadContextTurns(Long sessionId)
    {
        List<ChatMessage> latest = new ArrayList<>(
                messageRepository.findBySessionIdOrderByIdDesc(sessionId, Pageable.ofSize(contextWindowSize)));

        Collections.reverse(latest);

        List<Turn> turns = latest.stream()
                .map(m -> new Turn(m.getSender() == Sender.STUDENT ? "user" : "model", m.getContent()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Gemini expects the conversation to start with a user turn
        while (!turns.isEmpty() && !"user".equals(turns.get(0).role()))
        {
            turns.remove(0);
        }
        return turns;
    }

    private List<ChatSessionDto> toSessionDtos(List<ChatSession> sessions)
    {
        if (sessions.isEmpty())
        {
            return List.of();
        }
        List<Long> ids = sessions.stream().map(ChatSession::getId).toList();
        Map<Long, SessionMessageStats> stats = messageRepository.findStatsBySessionIds(ids).stream()
                .collect(Collectors.toMap(SessionMessageStats::getSessionId, Function.identity()));

        List<Long> lastIds = stats.values().stream().map(SessionMessageStats::getLastMessageId).toList();

        Map<Long, String> lastContentById = messageRepository.findAllById(lastIds).stream()
                .collect(Collectors.toMap(ChatMessage::getId, ChatMessage::getContent));

        return sessions.stream().map(session -> {
            SessionMessageStats s = stats.get(session.getId());
            return s == null
                    ? ChatSessionDto.from(session, null, 0)
                    : ChatSessionDto.from(session, lastContentById.get(s.getLastMessageId()), s.getMessageCount());
        }).toList();
    }


    private static String autoTitle(String firstMessage)
    {
        String text = firstMessage.strip();
        return text.length() <= AUTO_TITLE_LENGTH ? text : text.substring(0, AUTO_TITLE_LENGTH) + "...";
    }
}