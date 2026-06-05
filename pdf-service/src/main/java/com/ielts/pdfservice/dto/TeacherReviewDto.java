package com.ielts.pdfservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeacherReviewDto {

    @JsonProperty("teacher_feedback")
    private String teacherFeedback;

    @JsonProperty("pronunciation_score")
    private Double pronunciationScore;

    @JsonProperty("adjusted_fluency")
    private Double adjustedFluency;

    @JsonProperty("adjusted_lexical")
    private Double adjustedLexical;

    @JsonProperty("adjusted_grammar")
    private Double adjustedGrammar;

    @JsonProperty("final_overall_score")
    private Double finalOverallScore;

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String v) { this.teacherFeedback = v; }

    public Double getPronunciationScore() { return pronunciationScore; }
    public void setPronunciationScore(Double v) { this.pronunciationScore = v; }

    public Double getAdjustedFluency() { return adjustedFluency; }
    public void setAdjustedFluency(Double v) { this.adjustedFluency = v; }

    public Double getAdjustedLexical() { return adjustedLexical; }
    public void setAdjustedLexical(Double v) { this.adjustedLexical = v; }

    public Double getAdjustedGrammar() { return adjustedGrammar; }
    public void setAdjustedGrammar(Double v) { this.adjustedGrammar = v; }

    public Double getFinalOverallScore() { return finalOverallScore; }
    public void setFinalOverallScore(Double v) { this.finalOverallScore = v; }
}
