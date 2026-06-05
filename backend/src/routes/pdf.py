import os

import httpx
from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import Response
from sqlalchemy.orm import Session

from src.models.db_models import Submission, User
from src.services.auth_service import get_current_user
from src.utils.database import get_db

router = APIRouter()

_pdf_url_raw = os.getenv("PDF_SERVICE_URL", "http://localhost:8081")
PDF_SERVICE_URL = _pdf_url_raw if _pdf_url_raw.startswith("http") else f"https://{_pdf_url_raw}"


@router.get("/submissions/{submission_id}/pdf")
async def download_pdf_report(
    submission_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    from sqlalchemy.orm import joinedload

    submission = (
        db.query(Submission)
        .options(
            joinedload(Submission.question),
            joinedload(Submission.ai_evaluation),
            joinedload(Submission.teacher_review),
            joinedload(Submission.user),
        )
        .filter(Submission.id == submission_id)
        .first()
    )

    if not submission:
        raise HTTPException(status_code=404, detail="Submission not found")

    if current_user.role == "student" and submission.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    if not submission.ai_evaluation:
        raise HTTPException(status_code=400, detail="Evaluation not complete yet")

    ai = submission.ai_evaluation.raw_llm_response
    tr = submission.teacher_review

    teacher_review_payload = None
    if tr:
        teacher_review_payload = {
            "teacher_feedback": tr.teacher_feedback,
            "pronunciation_score": float(tr.pronunciation_score) if tr.pronunciation_score is not None else None,
            "adjusted_fluency": float(tr.adjusted_fluency) if tr.adjusted_fluency is not None else None,
            "adjusted_lexical": float(tr.adjusted_lexical) if tr.adjusted_lexical is not None else None,
            "adjusted_grammar": float(tr.adjusted_grammar) if tr.adjusted_grammar is not None else None,
            "final_overall_score": float(tr.final_overall_score) if tr.final_overall_score is not None else None,
        }

    payload = {
        "student_name": submission.user.full_name,
        "question_part": submission.question.part,
        "question_text": submission.question.question_text,
        "submitted_at": submission.submitted_at.isoformat() if submission.submitted_at else None,
        "transcript": submission.transcript,
        "ai_evaluation": ai,
        "teacher_review": teacher_review_payload,
    }

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.post(f"{PDF_SERVICE_URL}/api/pdf/generate-report", json=payload)
        if resp.status_code != 200:
            raise HTTPException(status_code=502, detail="PDF service error")
    except httpx.ConnectError:
        raise HTTPException(status_code=503, detail="PDF service unavailable")

    student_name = (submission.user.full_name or "Student").replace(" ", "_")
    filename = f"IELTS_Report_{student_name}.pdf"

    return Response(
        content=resp.content,
        media_type="application/pdf",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'},
    )
