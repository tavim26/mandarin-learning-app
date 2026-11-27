package com.chineselearning.progressservice.security;

import com.chineselearning.progressservice.exception.ForbiddenOperationException;
import org.springframework.stereotype.Component;

/** Single place for authorization rules; default-deny for unknown roles. */
@Component
public class ProgressAccessPolicy {

    public void assertCanViewStudent(CurrentUser user, Long studentId)
    {
        if (user.isStudent() && user.id().equals(studentId))
        {
            return;
        }
        if (user.isTeacher() || user.isAdmin())
        {
            return;
        }
        throw new ForbiddenOperationException("Access forbidden");
    }

    public void assertCanSubmitAttempts(CurrentUser user)
    {
        if (!user.isStudent())
        {
            throw new ForbiddenOperationException("Only students can submit exercise attempts");
        }
    }

    public void assertAdmin(CurrentUser user)
    {
        if (!user.isAdmin())
        {
            throw new ForbiddenOperationException("Access forbidden");
        }
    }
}