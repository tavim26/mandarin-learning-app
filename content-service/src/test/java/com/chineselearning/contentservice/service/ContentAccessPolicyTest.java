package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.entity.CourseUnitEntity;
import com.chineselearning.contentservice.entity.LessonEntity;
import com.chineselearning.contentservice.exception.ForbiddenOperationException;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import com.chineselearning.contentservice.repository.CourseUnitRepository;
import com.chineselearning.contentservice.repository.LessonRepository;
import com.chineselearning.contentservice.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentAccessPolicyTest
{
    private static final CurrentUser ADMIN = new CurrentUser(1L, "ADMIN");
    private static final CurrentUser OWNER = new CurrentUser(7L, "TEACHER");
    private static final CurrentUser OTHER_TEACHER = new CurrentUser(8L, "TEACHER");
    private static final CurrentUser STUDENT = new CurrentUser(9L, "STUDENT");

    @Mock CourseUnitRepository unitRepository;
    @Mock LessonRepository lessonRepository;


    @InjectMocks ContentAccessPolicy policy;

    private final CourseUnitEntity unit = new CourseUnitEntity("Greetings", null, 1, 0, 7L);

    @Test
    void aTeacherCanModifyHisOwnUnit()
    {
        when(unitRepository.findById(5L)).thenReturn(Optional.of(unit));

        assertSame(unit, policy.requireUnitOwner(OWNER, 5L));
    }

    @Test
    void anotherTeacherCannotModifyTheUnit()
    {
        when(unitRepository.findById(5L)).thenReturn(Optional.of(unit));

        assertThrows(ForbiddenOperationException.class, () -> policy.requireUnitOwner(OTHER_TEACHER, 5L));
    }

    @Test
    void anAdminCanModifyAnyUnit()
    {
        when(unitRepository.findById(5L)).thenReturn(Optional.of(unit));

        assertSame(unit, policy.requireUnitOwner(ADMIN, 5L));
    }

    @Test
    void aStudentIsRejectedBeforeAnyLookup()
    {
        assertThrows(ForbiddenOperationException.class, () -> policy.requireUnitOwner(STUDENT, 5L));

        verifyNoInteractions(unitRepository);
    }

    @Test
    void anUnknownUnitIsReportedAsNotFound()
    {
        when(unitRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policy.requireUnitOwner(OWNER, 5L));
    }

    @Test
    void lessonOwnershipIsDecidedByTheUnitItBelongsTo()
    {
        LessonEntity lesson = new LessonEntity(unit, "Hello", null, 10, 0);
        when(lessonRepository.findWithUnitById(3L)).thenReturn(Optional.of(lesson));

        assertSame(lesson, policy.requireLessonOwner(OWNER, 3L));
        assertThrows(ForbiddenOperationException.class, () -> policy.requireLessonOwner(OTHER_TEACHER, 3L));
    }
}