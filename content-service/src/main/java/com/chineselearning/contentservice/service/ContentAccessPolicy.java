package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.domain.CourseUnit;
import com.chineselearning.contentservice.domain.Exercise;
import com.chineselearning.contentservice.domain.Lesson;
import com.chineselearning.contentservice.domain.LessonMaterial;
import com.chineselearning.contentservice.domain.dao.ICourseUnitDao;
import com.chineselearning.contentservice.domain.dao.IExerciseDao;
import com.chineselearning.contentservice.domain.dao.ILessonDao;
import com.chineselearning.contentservice.domain.dao.ILessonMaterialDao;
import com.chineselearning.contentservice.exception.ForbiddenOperationException;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import com.chineselearning.contentservice.security.CurrentUser;
import org.springframework.stereotype.Component;

/**
 * Teachers may modify only the content of units they created; admins may modify everything.
 */
@Component
public class ContentAccessPolicy
{
    private final ICourseUnitDao courseUnitDao;
    private final ILessonDao lessonDao;
    private final IExerciseDao exerciseDao;
    private final ILessonMaterialDao lessonMaterialDao;

    public ContentAccessPolicy(ICourseUnitDao courseUnitDao, ILessonDao lessonDao,
                               IExerciseDao exerciseDao, ILessonMaterialDao lessonMaterialDao)
    {
        this.courseUnitDao = courseUnitDao;
        this.lessonDao = lessonDao;
        this.exerciseDao = exerciseDao;
        this.lessonMaterialDao = lessonMaterialDao;
    }

    public void requireEditor(CurrentUser user)
    {
        if (!user.canEditContent())
        {
            throw new ForbiddenOperationException("Only teachers and admins can modify course content");
        }
    }

    public CourseUnit requireUnitOwner(CurrentUser user, Long unitId)
    {
        requireEditor(user);
        CourseUnit unit = courseUnitDao.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseUnit not found: " + unitId));

        if (!user.isAdmin() && !user.id().equals(unit.getCreatedByTeacherId()))
        {
            throw new ForbiddenOperationException("You can only modify your own course units");
        }
        return unit;
    }

    public Lesson requireLessonOwner(CurrentUser user, Long lessonId)
    {
        requireEditor(user);
        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        requireUnitOwner(user, lesson.getUnit().getId());
        return lesson;
    }

    public Exercise requireExerciseOwner(CurrentUser user, Long exerciseId)
    {
        requireEditor(user);
        Exercise exercise = exerciseDao.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + exerciseId));
        requireLessonOwner(user, exercise.getLesson().getId());
        return exercise;
    }

    public LessonMaterial requireMaterialOwner(CurrentUser user, Long materialId)
    {
        requireEditor(user);
        LessonMaterial material = lessonMaterialDao.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonMaterial not found: " + materialId));
        requireLessonOwner(user, material.getLesson().getId());
        return material;
    }
}