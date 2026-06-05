package com.ielts.pdfservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiEvaluationDto {

    @JsonProperty("fluency_coherence")
    private CriteriaScoreDto fluencyCoherence;

    @JsonProperty("lexical_resource")
    private CriteriaScoreDto lexicalResource;

    @JsonProperty("grammar")
    private CriteriaScoreDto grammar;

    @JsonProperty("pronunciation")
    private CriteriaScoreDto pronunciation;

    @JsonProperty("overall_band")
    private Double overallBand;

    @JsonProperty("key_mistakes")
    private List<String> keyMistakes;

    @JsonProperty("improvement_suggestions")
    private List<String> improvementSuggestions;

    public CriteriaScoreDto getFluencyCoherence() { return fluencyCoherence; }
    public void setFluencyCoherence(CriteriaScoreDto v) { this.fluencyCoherence = v; }

    public CriteriaScoreDto getLexicalResource() { return lexicalResource; }
    public void setLexicalResource(CriteriaScoreDto v) { this.lexicalResource = v; }

    public CriteriaScoreDto getGrammar() { return grammar; }
    public void setGrammar(CriteriaScoreDto v) { this.grammar = v; }

    public CriteriaScoreDto getPronunciation() { return pronunciation; }
    public void setPronunciation(CriteriaScoreDto v) { this.pronunciation = v; }

    public Double getOverallBand() { return overallBand; }
    public void setOverallBand(Double v) { this.overallBand = v; }

    public List<String> getKeyMistakes() { return keyMistakes; }
    public void setKeyMistakes(List<String> v) { this.keyMistakes = v; }

    public List<String> getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(List<String> v) { this.improvementSuggestions = v; }
}
