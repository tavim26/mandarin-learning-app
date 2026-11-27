package com.chineselearning.progressservice.service;

import com.chineselearning.progressservice.client.ContentClient;
import com.chineselearning.progressservice.client.ExerciseView;
import com.chineselearning.progressservice.client.LessonView;
import com.chineselearning.progressservice.dto.ExerciseAttemptDto;
import com.chineselearning.progressservice.dto.SubmitAttemptRequest;
import com.chineselearning.progressservice.entity.ExerciseAttempt;
import com.chineselearning.progressservice.entity.StudentLessonProgress;
import com.chineselearning.progressservice.entity.StudentReplica;
import com.chineselearning.progressservice.evaluation.EvaluationResult;
import com.chineselearning.progressservice.evaluation.EvaluationService;
import com.chineselearning.progressservice.exception.ResourceNotFoundException;
import com.chineselearning.progressservice.repository.ExerciseAttemptRepository;
import com.chineselearning.progressservice.repository.StudentLessonProgressRepository;
import com.chineselearning.progressservice.repository.StudentReplicaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttemptService {

    private static final Logger log = LoggerFactory.getLogger(AttemptService.class);

    private final ContentClient contentClient;
    private final EvaluationService evaluationService;
    private final StudentReplicaRepository replicaRepository;
    private final ExerciseAttemptRepository attemptRepository;
    private final StudentLessonProgressRepository progressRepository;
    private final TransactionTemplate writeTx;

    public AttemptService(ContentClient contentClient,
                          EvaluationService evaluationService,
                          StudentReplicaRepository replicaRepository,
                          ExerciseAttemptRepository attemptRepository,
                          StudentLessonProgressRepository progressRepository,
                          PlatformTransactionManager transactionManager) {
        this.contentClient = contentClient;
        this.evaluationService = evaluationService;
        this.replicaRepository = replicaRepository;
        this.attemptRepository = attemptRepository;
        this.progressRepository = progressRepository;
        this.writeTx = new TransactionTemplate(transactionManager);
    }

    /**
     * Deliberately NOT @Transactional: the content-service calls must not hold a DB connection.
     */
    public ExerciseAttemptDto submitAttempt(Long studentId, SubmitAttemptRequest request) {
        // 1) remote reads, no transaction
        ExerciseView exercise = contentClient.getExercise(request.exerciseId());
        LessonView lesson = contentClient.getLesson(exercise.lessonId());

        // 2) pure evaluation (400 for malformed answers, 500 for malformed exercises: nothing is saved)
        EvaluationResult result = evaluationService.evaluate(
                exercise.type(), exercise.contentData(), request.submittedAnswer());

        // 3) one short transaction, serialized per student
        return writeTx.execute(status -> persist(studentId, request, lesson, result));
    }

    private ExerciseAttemptDto persist(Long studentId, SubmitAttemptRequest request,
                                       LessonView lesson, EvaluationResult result) {
        replicaRepository.insertIfAbsent(studentId);
        StudentReplica replica = replicaRepository.findByIdForUpdate(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student replica " + studentId + " not found"));

        int attemptNumber = attemptRepository.countByStudentIdAndExerciseId(studentId, request.exerciseId()) + 1;
        ExerciseAttempt saved = attemptRepository.save(ExerciseAttempt.record(
                studentId, request.exerciseId(), attemptNumber, request.submittedAnswer(),
                result.isCorrect(), result.score(), result.feedback()));
        log.info("Saved attempt studentId={}, exerciseId={}, attemptNumber={}, correct={}",
                studentId, request.exerciseId(), attemptNumber, result.isCorrect());

        updateLessonProgress(studentId, lesson, replica);
        return ExerciseAttemptDto.from(saved);
    }

    private void updateLessonProgress(Long studentId, LessonView lesson, StudentReplica replica) {
        if (lesson.exercises() == null || lesson.exercises().isEmpty()) {
            log.warn("Lesson {} has no exercises, progress is not updated", lesson.id());
            return;
        }
        List<Long> exerciseIds = lesson.exercises().stream().map(ExerciseView::id).toList();
        long correctCount = attemptRepository.countDistinctCorrectExercises(studentId, exerciseIds);
        BigDecimal completionPct = BigDecimal.valueOf((correctCount * 100.0) / exerciseIds.size())
                .setScale(2, RoundingMode.HALF_UP);

        StudentLessonProgress progress = progressRepository
                .findByStudentIdAndLessonId(studentId, lesson.id())
                .orElseGet(() -> new StudentLessonProgress(studentId, lesson.id()));

        boolean grantXp = progress.updateCompletion(completionPct, LocalDateTime.now());
        if (grantXp) {
            int xp = lesson.xpReward() == null ? 0 : lesson.xpReward();
            progress.grantXp(xp);
            replica.addXp(xp); // replica is locked: no lost update, no double award
            log.info("XP granted: studentId={}, lessonId={}, xp={}, totalXp={}", studentId, lesson.id(), xp,
                    replica.getXpTotal());
        }
        progressRepository.save(progress);
    }
}