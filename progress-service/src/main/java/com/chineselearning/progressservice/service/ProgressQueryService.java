package com.chineselearning.progressservice.service;

import com.chineselearning.progressservice.client.ContentClient;
import com.chineselearning.progressservice.client.LessonView;
import com.chineselearning.progressservice.dto.ExerciseAttemptDto;
import com.chineselearning.progressservice.dto.StudentLessonProgressDto;
import com.chineselearning.progressservice.dto.StudentSummaryDto;
import com.chineselearning.progressservice.dto.StudentUnitProgressDto;
import com.chineselearning.progressservice.entity.LessonStatus;
import com.chineselearning.progressservice.entity.StudentLessonProgress;
import com.chineselearning.progressservice.entity.StudentReplica;
import com.chineselearning.progressservice.exception.ResourceNotFoundException;
import com.chineselearning.progressservice.repository.ExerciseAttemptRepository;
import com.chineselearning.progressservice.repository.StudentLessonProgressRepository;
import com.chineselearning.progressservice.repository.StudentReplicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProgressQueryService {

    private final ExerciseAttemptRepository attemptRepository;
    private final StudentLessonProgressRepository progressRepository;
    private final StudentReplicaRepository replicaRepository;
    private final ContentClient contentClient;

    public ProgressQueryService(ExerciseAttemptRepository attemptRepository,
                                StudentLessonProgressRepository progressRepository,
                                StudentReplicaRepository replicaRepository,
                                ContentClient contentClient) {
        this.attemptRepository = attemptRepository;
        this.progressRepository = progressRepository;
        this.replicaRepository = replicaRepository;
        this.contentClient = contentClient;
    }

    @Transactional(readOnly = true)
    public List<ExerciseAttemptDto> getAttempts(Long studentId, Long exerciseId) {
        return attemptRepository.findByStudentIdAndExerciseIdOrderByAttemptNumberAsc(studentId, exerciseId).stream()
                .map(ExerciseAttemptDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentLessonProgressDto getLessonProgress(Long studentId, Long lessonId) {
        return progressRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .map(StudentLessonProgressDto::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No progress for studentId=" + studentId + ", lessonId=" + lessonId));
    }

    @Transactional(readOnly = true)
    public List<StudentLessonProgressDto> getAllProgress(Long studentId) {
        return progressRepository.findByStudentIdOrderByLessonIdAsc(studentId).stream()
                .map(StudentLessonProgressDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentLessonProgressDto> getInProgress(Long studentId) {
        return progressRepository.findByStudentIdAndStatusOrderByLessonIdAsc(studentId, LessonStatus.IN_PROGRESS)
                .stream()
                .map(StudentLessonProgressDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentLessonProgressDto> getLessonLeaderboard(Long lessonId) {
        return progressRepository.findTop10ByLessonIdOrderByCompletionPctDescIdAsc(lessonId).stream()
                .map(StudentLessonProgressDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentSummaryDto getSummary(Long studentId) {
        StudentReplica replica = replicaRepository.findById(studentId).orElseGet(() -> new StudentReplica(studentId));
        return new StudentSummaryDto(
                studentId,
                replica.getXpTotal(),
                replica.getLevel(),
                progressRepository.countByStudentIdAndStatus(studentId, LessonStatus.COMPLETED),
                progressRepository.countByStudentIdAndStatus(studentId, LessonStatus.IN_PROGRESS));
    }

    /**
     * Not @Transactional: it first calls content-service, and a connection must not be held during that call.
     * The repository call below runs in its own short read-only transaction.
     */
    public StudentUnitProgressDto getUnitProgress(Long studentId, Long unitId) {
        List<LessonView> lessons = contentClient.getLessonsForUnit(unitId);
        if (lessons.isEmpty()) {
            return new StudentUnitProgressDto(unitId, studentId, 0, 0, 0, 0, BigDecimal.ZERO);
        }
        List<Long> lessonIds = lessons.stream().map(LessonView::id).toList();
        List<StudentLessonProgress> progresses = progressRepository.findByStudentIdAndLessonIdIn(studentId, lessonIds);

        long completed = progresses.stream().filter(p -> p.getStatus() == LessonStatus.COMPLETED).count();
        long inProgress = progresses.stream().filter(p -> p.getStatus() == LessonStatus.IN_PROGRESS).count();
        long notStarted = lessons.size() - completed - inProgress;
        BigDecimal pct = BigDecimal.valueOf((completed * 100.0) / lessons.size()).setScale(2, RoundingMode.HALF_UP);

        return new StudentUnitProgressDto(unitId, studentId, lessons.size(), completed, inProgress, notStarted, pct);
    }
}