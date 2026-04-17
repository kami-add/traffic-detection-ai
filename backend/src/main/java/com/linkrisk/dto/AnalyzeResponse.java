package com.linkrisk.dto;

import java.util.List;
import java.util.Map;

public class AnalyzeResponse {
    private int score;
    private String level;
    private Map<String, Object> features;
    private Map<String, Double> probabilities;
    private Map<String, Integer> radar;
    private List<String> logs;
    private List<String> advice;

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Map<String, Object> getFeatures() { return features; }
    public void setFeatures(Map<String, Object> features) { this.features = features; }
    public Map<String, Double> getProbabilities() { return probabilities; }
    public void setProbabilities(Map<String, Double> probabilities) { this.probabilities = probabilities; }
    public List<String> getLogs() { return logs; }
    public void setLogs(List<String> logs) { this.logs = logs; }
    public List<String> getAdvice() { return advice; }
    public void setAdvice(List<String> advice) { this.advice = advice; }
    public Map<String, Integer> getRadar() { return radar; }
    public void setRadar(Map<String, Integer> radar) { this.radar = radar; }
}
