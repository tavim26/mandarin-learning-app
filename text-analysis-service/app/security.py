# app/security.py
from dataclasses import dataclass
from enum import StrEnum

from app.errors import ForbiddenError


class Role(StrEnum):
    STUDENT = "STUDENT"
    TEACHER = "TEACHER"
    ADMIN = "ADMIN"


@dataclass(frozen=True)
class CurrentUser:
    id: int
    role: Role

    @classmethod
    def from_headers(cls, user_id: int, role: str) -> "CurrentUser":
        try:
            return cls(id=user_id, role=Role(role))
        except ValueError:
            raise ForbiddenError("Unknown user role") from None


class AnalysisAccessPolicy:
    """Who may use the text analysis module and whose analyses they may see."""

    def assert_can_use_module(self, user: CurrentUser) -> None:
        if user.role is Role.TEACHER:
            raise ForbiddenError("Teachers cannot access the text analysis module")

    def assert_can_access_student(self, user: CurrentUser, student_id: int) -> None:
        self.assert_can_use_module(user)
        if user.role is Role.STUDENT and user.id != student_id:
            raise ForbiddenError("You cannot access another student's resources")