package com.ielts.pdfservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportRequest {

    @JsonProperty("student_name")
    private String studentName;

    @JsonProperty("question_part")
    private String questionPart;

    @JsonProperty("question_text")
    private String questionText;

    @JsonProperty("submitted_at")
    private String submittedAt;

    @JsonProperty("transcript")
    private String transcript;

    @JsonProperty("ai_evaluation")
    private AiEvaluationDto aiEvaluation;

    @JsonProperty("teacher_review")
    private TeacherReviewDto teacherReview;

    public String getStudentName() { return studentName; }
    public void setStudentName(String v) { this.studentName = v; }

    public String getQuestionPart() { return questionPart; }
    public void setQuestionPart(String v) { this.questionPart = v; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String v) { this.questionText = v; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String v) { this.submittedAt = v; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String v) { this.transcript = v; }

    public AiEvaluationDto getAiEvaluation() { return aiEvaluation; }
    public void setAiEvaluation(AiEvaluationDto v) { this.aiEvaluation = v; }

    public TeacherReviewDto getTeacherReview() { return teacherReview; }
    public void setTeacherReview(TeacherReviewDto v) { this.teacherReview = v; }
}
