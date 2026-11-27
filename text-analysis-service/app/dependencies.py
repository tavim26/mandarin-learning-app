# app/dependencies.py
from dataclasses import dataclass
from typing import Annotated, Iterator

from fastapi import Depends, Header, Request
from sqlalchemy.orm import Session, sessionmaker

from app.config import Settings
from app.database import session_scope
from app.errors import UnauthorizedError
from app.repositories.analysis_repository import AnalysisRepository
from app.security import AnalysisAccessPolicy, CurrentUser
from app.services.analysis_service import AnalysisService
from app.services.hsk import HskVocabulary
from app.services.nlp import NlpProcessor
from app.services.ocr import OcrReader
from app.services.rate_limiter import SlidingWindowRateLimiter
from app.services.stats_service import StatsService
from app.services.translation import GoogleTranslateClient


@dataclass(frozen=True)
class Container:

    settings: Settings
    session_factory: sessionmaker[Session]
    vocabulary: HskVocabulary
    nlp: NlpProcessor
    ocr: OcrReader
    translator: GoogleTranslateClient
    policy: AnalysisAccessPolicy
    analysis_limiter: SlidingWindowRateLimiter
    preview_limiter: SlidingWindowRateLimiter


def get_container(request: Request) -> Container:
    return request.app.state.container


ContainerDep = Annotated[Container, Depends(get_container)]


def get_session(container: ContainerDep) -> Iterator[Session]:
    yield from session_scope(container.session_factory)


SessionDep = Annotated[Session, Depends(get_session)]


def get_current_user(
    x_user_id: Annotated[int | None, Header(alias="X-User-Id")] = None,
    x_user_role: Annotated[str | None, Header(alias="X-User-Role")] = None,
) -> CurrentUser:
    # The gateway validates the JWT and sets these headers.
    if x_user_id is None or not x_user_role:
        raise UnauthorizedError("Missing user identity")
    return CurrentUser.from_headers(x_user_id, x_user_role)


CurrentUserDep = Annotated[CurrentUser, Depends(get_current_user)]


def get_analysis_service(container: ContainerDep, session: SessionDep) -> AnalysisService:
    return AnalysisService(
        session=session,
        repository=AnalysisRepository(session),
        nlp=container.nlp,
        translator=container.translator,
        policy=container.policy,
    )


def get_stats_service(container: ContainerDep, session: SessionDep) -> StatsService:
    return StatsService(AnalysisRepository(session), container.vocabulary)


AnalysisServiceDep = Annotated[AnalysisService, Depends(get_analysis_service)]
StatsServiceDep = Annotated[StatsService, Depends(get_stats_service)]