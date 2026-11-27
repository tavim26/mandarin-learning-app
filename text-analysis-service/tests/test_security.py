# tests/test_security.py
import pytest

from app.errors import ForbiddenError
from app.security import AnalysisAccessPolicy, CurrentUser, Role

policy = AnalysisAccessPolicy()
student = CurrentUser(7, Role.STUDENT)


def test_student_can_access_own_data_only():
    policy.assert_can_access_student(student, 7)
    with pytest.raises(ForbiddenError):
        policy.assert_can_access_student(student, 8)


def test_teacher_has_no_access_at_all():
    with pytest.raises(ForbiddenError):
        policy.assert_can_access_student(CurrentUser(1, Role.TEACHER), 7)
    with pytest.raises(ForbiddenError):
        policy.assert_can_use_module(CurrentUser(1, Role.TEACHER))


def test_admin_can_access_any_student():
    policy.assert_can_access_student(CurrentUser(1, Role.ADMIN), 7)


def test_unknown_role_is_rejected():
    with pytest.raises(ForbiddenError):
        CurrentUser.from_headers(1, "SUPERUSER")