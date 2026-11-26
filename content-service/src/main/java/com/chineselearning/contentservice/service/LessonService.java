package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.dto.ExerciseDto;
import com.chineselearning.contentservice.dto.LessonDto;
import com.chineselearning.contentservice.dto.LessonRequest;
import com.chineselearning.contentservice.entity.CourseUnitEntity;
import com.chineselearning.contentservice.entity.LessonEntity;
import com.chineselearning.contentservice.entity.LessonMaterialEntity;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import com.chineselearning.contentservice.repository.ExerciseRepository;
import com.chineselearning.contentservice.repository.LessonMaterialRepository;
import com.chineselearning.contentservice.repository.LessonRepository;
import com.chineselearning.contentservice.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LessonService
{
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final LessonMaterialRepository materialRepository;
    private final ContentAccessPolicy accessPolicy;
    private final StorageService storageService;

    public LessonService(LessonRepository lessonRepository, ExerciseRepository exerciseRepository,
                         LessonMaterialRepository materialRepository, ContentAccessPolicy accessPolicy,
                         StorageService storageService)
    {
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.materialRepository = materialRepository;
        this.accessPolicy = accessPolicy;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<LessonDto> findByUnit(Long unitId)
    {
        return lessonRepository.findByUnitIdOrderByOrderIndexAsc(unitId).stream()
                .map(LessonDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LessonDto get(Long id)
    {
        LessonEntity lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + id));

        List<ExerciseDto> exercises = exerciseRepository.findByLessonIdOrderByIdAsc(id).stream()
                .map(ExerciseDto::from)
                .toList();

        return LessonDto.from(lesson).withExercises(exercises);
    }

    @Transactional
    public LessonDto create(LessonRequest request, CurrentUser user)
    {
        CourseUnitEntity unit = accessPolicy.requireUnitOwner(user, request.unitId());

        LessonEntity lesson = new LessonEntity(unit, request.title(), request.description(),
                request.xpReward(), request.orderIndex());

        return LessonDto.from(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonDto update(Long id, LessonRequest request, CurrentUser user)
    {
        LessonEntity lesson = accessPolicy.requireLessonOwner(user, id);

        // Moving a lesson requires ownership of the target unit as well.
        if (!request.unitId().equals(lesson.getUnit().getId()))
        {
            lesson.moveTo(accessPolicy.requireUnitOwner(user, request.unitId()));
        }
        lesson.update(request.title(), request.description(), request.xpReward(), request.orderIndex());

        return LessonDto.from(lesson);
    }

    @Transactional
    public void delete(Long id, CurrentUser user)
    {
        LessonEntity lesson = accessPolicy.requireLessonOwner(user, id);
        List<LessonMaterialEntity> materials = materialRepository.findByLessonIdOrderByIdAsc(id);

        lessonRepository.delete(lesson);
        storageService.deleteFilesAfterCommit(materials);
    }
}