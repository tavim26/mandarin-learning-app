# app/routers/analysis.py
import math
from typing import Annotated

from fastapi import APIRouter, File, Form, Query, Response, UploadFile, status

from app.dependencies import AnalysisServiceDep, ContainerDep, CurrentUserDep, StatsServiceDep
from app.errors import PayloadTooLargeError
from app.schemas.analysis import (
    AnalysisResponse,
    AnalysisSummaryResponse,
    AnalyzeTextRequest,
    Page,
    PreviewRequest,
    PreviewResponse,
    PreviewTokenResponse,
    SortOrder,
    SourceType,
    StudentStatsResponse,
    TranslationLanguage,
)

router = APIRouter(prefix="/api/analysis", tags=["analysis"])


@router.post("/text", response_model=AnalysisResponse, status_code=status.HTTP_201_CREATED)
def analyze_text(
    request: AnalyzeTextRequest,
    user: CurrentUserDep,
    container: ContainerDep,
    service: AnalysisServiceDep,
):
    container.policy.assert_can_use_module(user)
    container.analysis_limiter.check(user.id)
    return service.analyze(user.id, request.raw_text, "MANUAL", request.translation_language)


@router.post("/ocr", response_model=AnalysisResponse, status_code=status.HTTP_201_CREATED)
def analyze_image(
    image: Annotated[UploadFile, File()],
    user: CurrentUserDep,
    container: ContainerDep,
    service: AnalysisServiceDep,
    translation_language: Annotated[TranslationLanguage, Form()] = "en",
):
    container.policy.assert_can_use_module(user)
    container.analysis_limiter.check(user.id)

    max_bytes = container.settings.max_upload_bytes
    image_bytes = image.file.read(max_bytes + 1)  # one extra byte tells us the file is too big
    if len(image_bytes) > max_bytes:
        raise PayloadTooLargeError(f"The image must not exceed {max_bytes // (1024 * 1024)} MB")

    text = container.ocr.extract_text(image_bytes)
    return service.analyze(user.id, text, "OCR", translation_language)


@router.post("/preview", response_model=PreviewResponse)
def preview_text(
    request: PreviewRequest,
    user: CurrentUserDep,
    container: ContainerDep,
    service: AnalysisServiceDep,
):
    container.policy.assert_can_use_module(user)
    container.preview_limiter.check(user.id)
    return PreviewResponse(
        tokens=[
            PreviewTokenResponse(
                hanzi=word.hanzi,
                pinyin=word.pinyin,
                hsk_level=word.hsk_level,
                position_index=word.position_index,
                pos=word.pos,
            )
            for word in service.preview(request.text)
        ]
    )


@router.get("/student/{student_id}/stats", response_model=StudentStatsResponse)
def get_student_stats(
    student_id: int,
    user: CurrentUserDep,
    container: ContainerDep,
    stats: StatsServiceDep,
):
    container.policy.assert_can_access_student(user, student_id)
    return stats.for_student(student_id)


@router.get("/student/{student_id}", response_model=Page[AnalysisSummaryResponse])
def list_student_analyses(
    student_id: int,
    user: CurrentUserDep,
    container: ContainerDep,
    service: AnalysisServiceDep,
    page: Annotated[int, Query(ge=1)] = 1,
    size: Annotated[int, Query(ge=1, le=100)] = 20,
    source_type: SourceType | None = None,
    hsk_level: Annotated[int | None, Query(ge=1, le=6)] = None,
    sort_order: SortOrder = "newest",
):
    container.policy.assert_can_access_student(user, student_id)
    analyses, total = service.list_for_student(
        student_id,
        page=page,
        size=size,
        source_type=source_type,
        hsk_level=hsk_level,
        sort_order=sort_order,
    )
    return Page[AnalysisSummaryResponse](
        items=[AnalysisSummaryResponse.model_validate(analysis) for analysis in analyses],
        total=total,
        page=page,
        size=size,
        total_pages=math.ceil(total / size),
    )


@router.get("/{analysis_id}", response_model=AnalysisResponse)
def get_analysis(analysis_id: int, user: CurrentUserDep, service: AnalysisServiceDep):
    return service.get(user, analysis_id)


@router.delete("/{analysis_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_analysis(analysis_id: int, user: CurrentUserDep, service: AnalysisServiceDep) -> Response:
    service.delete(user, analysis_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)