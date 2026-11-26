package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.dto.ExerciseDto;
import com.chineselearning.contentservice.dto.ExerciseRequest;
import com.chineselearning.contentservice.dto.LessonExerciseTypesDto;
import com.chineselearning.contentservice.entity.ExerciseEntity;
import com.chineselearning.contentservice.entity.LessonEntity;
import com.chineselearning.contentservice.exception.InvalidRequestException;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import com.chineselearning.contentservice.repository.ExerciseRepository;
import com.chineselearning.contentservice.repository.LessonRepository;
import com.chineselearning.contentservice.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExerciseService
{
    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonRepository;
    private final ContentAccessPolicy accessPolicy;

    public ExerciseService(ExerciseRepository exerciseRepository, LessonRepository lessonRepository, ContentAccessPolicy accessPolicy)
    {
        this.exerciseRepository = exerciseRepository;
        this.lessonRepository = lessonRepository;
        this.accessPolicy = accessPolicy;
    }

    @Transactional(readOnly = true)
    public ExerciseDto get(Long id)
    {
        return exerciseRepository.findById(id)
                .map(ExerciseDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto> findByLesson(Long lessonId)
    {
        return exerciseRepository.findByLessonIdOrderByIdAsc(lessonId).stream()
                .map(ExerciseDto::from)
                .toList();
    }

    @Transactional
    public ExerciseDto add(ExerciseRequest request, CurrentUser user)
    {
        if (request.lessonId() == null)
        {
            throw new InvalidRequestException("Lesson id is mandatory");
        }
        LessonEntity lesson = accessPolicy.requireLessonOwner(user, request.lessonId());
        ExerciseContentValidator.validate(request.type(), request.contentData());

        ExerciseEntity exercise = new ExerciseEntity(lesson, request.type(), request.prompt(), request.difficulty(), request.contentData());

        return ExerciseDto.from(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseDto update(Long id, ExerciseRequest request, CurrentUser user)
    {
        ExerciseEntity exercise = accessPolicy.requireExerciseOwner(user, id);
        ExerciseContentValidator.validate(request.type(), request.contentData());

        exercise.update(request.type(), request.prompt(), request.difficulty(), request.contentData());
        return ExerciseDto.from(exercise);
    }

    @Transactional
    public void delete(Long id, CurrentUser user)
    {
        exerciseRepository.delete(accessPolicy.requireExerciseOwner(user, id));
    }

    @Transactional(readOnly = true)
    public LessonExerciseTypesDto getTypeDistribution(Long lessonId)
    {
        if (!lessonRepository.existsById(lessonId))
        {
            throw new ResourceNotFoundException("Lesson not found: " + lessonId);
        }

        Map<String, Long> types = new LinkedHashMap<>();
        exerciseRepository.countByType(lessonId).forEach(row -> types.put(row.getExerciseType(), row.getTotal()));

        return new LessonExerciseTypesDto(lessonId, types);
    }
}