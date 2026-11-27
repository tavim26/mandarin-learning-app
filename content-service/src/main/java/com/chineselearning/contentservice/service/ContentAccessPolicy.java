package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.entity.CourseUnitEntity;
import com.chineselearning.contentservice.entity.ExerciseEntity;
import com.chineselearning.contentservice.entity.LessonEntity;
import com.chineselearning.contentservice.entity.LessonMaterialEntity;
import com.chineselearning.contentservice.exception.ForbiddenOperationException;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import com.chineselearning.contentservice.repository.CourseUnitRepository;
import com.chineselearning.contentservice.repository.ExerciseRepository;
import com.chineselearning.contentservice.repository.LessonMaterialRepository;
import com.chineselearning.contentservice.repository.LessonRepository;
import com.chineselearning.contentservice.security.CurrentUser;
import org.springframework.stereotype.Component;

/**
 * Teachers may modify only the content of units they created; admins may modify everything.
 * The require* methods that load entities must run inside the caller's transaction.
 */
@Component
public class ContentAccessPolicy
{
    private final CourseUnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final LessonMaterialRepository materialRepository;

    public ContentAccessPolicy(CourseUnitRepository unitRepository, LessonRepository lessonRepository, ExerciseRepository exerciseRepository, LessonMaterialRepository materialRepository)
    {
        this.unitRepository = unitRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.materialRepository = materialRepository;
    }

    public void requireEditor(CurrentUser user)
    {
        if (!user.canEditContent())
        {
            throw new ForbiddenOperationException("Only teachers and admins can modify course content");
        }
    }

    public CourseUnitEntity requireUnitOwner(CurrentUser user, Long unitId)
    {
        requireEditor(user);
        CourseUnitEntity unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseUnit not found: " + unitId));

        requireOwnership(user, unit);
        return unit;
    }

    public LessonEntity requireLessonOwner(CurrentUser user, Long lessonId)
    {
        requireEditor(user);
        LessonEntity lesson = lessonRepository.findWithUnitById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        requireOwnership(user, lesson.getUnit());
        return lesson;
    }

    public ExerciseEntity requireExerciseOwner(CurrentUser user, Long exerciseId)
    {
        requireEditor(user);
        ExerciseEntity exercise = exerciseRepository.findWithUnitById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + exerciseId));
        requireOwnership(user, exercise.getLesson().getUnit());
        return exercise;
    }

    public LessonMaterialEntity requireMaterialOwner(CurrentUser user, Long materialId)
    {
        requireEditor(user);
        LessonMaterialEntity material = materialRepository.findWithUnitById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonMaterial not found: " + materialId));
        requireOwnership(user, material.getLesson().getUnit());
        return material;
    }

    private static void requireOwnership(CurrentUser user, CourseUnitEntity unit)
    {
        if (!user.isAdmin() && !user.id().equals(unit.getCreatedByTeacherId()))
        {
            throw new ForbiddenOperationException("You can only modify your own course units");
        }
    }
}