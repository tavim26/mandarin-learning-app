package com.chineselearning.progressservice.security;

import com.chineselearning.progressservice.exception.ForbiddenOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProgressAccessPolicyTest {

    private final ProgressAccessPolicy policy = new ProgressAccessPolicy();

    @Test
    void studentSeesOnlyOwnData() {
        CurrentUser student = new CurrentUser(5L, "STUDENT");
        assertDoesNotThrow(() -> policy.assertCanViewStudent(student, 5L));
        assertThrows(ForbiddenOperationException.class, () -> policy.assertCanViewStudent(student, 6L));
    }

    @Test
    void unknownRoleIsDenied() {
        assertThrows(ForbiddenOperationException.class,
                () -> policy.assertCanViewStudent(new CurrentUser(1L, "HACKER"), 1L));
    }

    @Test
    void onlyStudentsSubmit() {
        assertThrows(ForbiddenOperationException.class,
                () -> policy.assertCanSubmitAttempts(new CurrentUser(1L, "TEACHER")));
    }
}