package com.chineselearning.flashcardservice.service;

import com.chineselearning.flashcardservice.dto.DueCountBySetDto;
import com.chineselearning.flashcardservice.dto.FlashcardSetStatsDto;
import com.chineselearning.flashcardservice.dto.TotalDueStatsDto;
import com.chineselearning.flashcardservice.entity.FlashcardProgress;
import com.chineselearning.flashcardservice.entity.FlashcardSet;
import com.chineselearning.flashcardservice.repository.FlashcardProgressRepository;
import com.chineselearning.flashcardservice.repository.FlashcardRepository;
import com.chineselearning.flashcardservice.repository.FlashcardSetRepository;
import com.chineselearning.flashcardservice.repository.SetCardCount;
import com.chineselearning.flashcardservice.repository.SetProgressCounts;
import com.chineselearning.flashcardservice.scheduling.Sm2Scheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudyStatsService {

    private static final int MATURE_INTERVAL_DAYS = 21;

    private final FlashcardSetRepository setRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository progressRepository;
    private final FlashcardAccess access;
    private final Clock clock;

    public StudyStatsService(FlashcardSetRepository setRepository,
                             FlashcardRepository flashcardRepository,
                             FlashcardProgressRepository progressRepository,
                             FlashcardAccess access,
                             Clock clock) {
        this.setRepository = setRepository;
        this.flashcardRepository = flashcardRepository;
        this.progressRepository = progressRepository;
        this.access = access;
        this.clock = clock;
    }

    public FlashcardSetStatsDto getSetStats(Long userId, Long setId)
    {
        access.requireOwnedSet(userId, setId);

        List<Long> cardIds = flashcardRepository.findIdsBySetId(setId);

        if (cardIds.isEmpty())
        {
            return new FlashcardSetStatsDto(setId, 0, 0, 0, 0, 0, Sm2Scheduler.INITIAL_EASINESS_FACTOR);
        }

        List<FlashcardProgress> progress = progressRepository.findByStudentIdAndFlashcardIdIn(userId, cardIds);
        LocalDateTime now = LocalDateTime.now(clock);

        int total = cardIds.size();
        int newCards = total - progress.size();
        int learning = (int) progress.stream().filter(p -> p.getIntervalDays() < MATURE_INTERVAL_DAYS).count();
        int mature = (int) progress.stream().filter(p -> p.getIntervalDays() >= MATURE_INTERVAL_DAYS).count();
        int dueExisting = (int) progress.stream()
                .filter(p -> p.getNextReviewAt() != null && !p.getNextReviewAt().isAfter(now))
                .count();

        BigDecimal averageEf = progress.isEmpty()
                ? Sm2Scheduler.INITIAL_EASINESS_FACTOR
                : progress.stream()
                .map(FlashcardProgress::getEasinessFactor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(progress.size()), 2, RoundingMode.HALF_UP);

        return new FlashcardSetStatsDto(setId, total, newCards, learning, mature, dueExisting + newCards, averageEf);
    }

    public TotalDueStatsDto getTotalDueStats(Long userId)
    {
        List<FlashcardSet> sets = setRepository.findByStudentIdOrderByIdDesc(userId);

        if (sets.isEmpty())
        {
            return new TotalDueStatsDto(0, List.of());
        }

        List<Long> setIds = sets.stream().map(FlashcardSet::getId).toList();
        LocalDateTime now = LocalDateTime.now(clock);

        Map<Long, Long> totals = flashcardRepository.countBySetIds(setIds).stream()
                .collect(Collectors.toMap(SetCardCount::getSetId, SetCardCount::getTotal));
        Map<Long, SetProgressCounts> progress = progressRepository.countProgressBySet(userId, setIds, now).stream()
                .collect(Collectors.toMap(SetProgressCounts::getSetId, Function.identity()));

        List<DueCountBySetDto> bySet = sets.stream()
                .map(set -> {
                    long total = totals.getOrDefault(set.getId(), 0L);
                    SetProgressCounts counts = progress.get(set.getId());
                    long seen = counts == null ? 0 : counts.getSeen();
                    long dueExisting = counts == null ? 0 : counts.getDue();
                    return new DueCountBySetDto(set.getId(), set.getTitle(), (int) ((total - seen) + dueExisting));
                })
                .filter(dto -> dto.dueCount() > 0)
                .toList();

        return new TotalDueStatsDto(bySet.stream().mapToInt(DueCountBySetDto::dueCount).sum(), bySet);
    }
}