package com.linkrisk.service.impl;

import com.linkrisk.dto.AnalyzeResponse;
import com.linkrisk.entity.DetectionRecord;
import com.linkrisk.repository.DetectionRecordRepository;
import com.linkrisk.service.AnalysisService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final Pattern IP_PATTERN = Pattern.compile("^(https?://)?(\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?(/.*)?$");
    private static final List<String> SUSPICIOUS_KEYWORDS = List.of("login", "verify", "bank", "secure", "password", "account", "update");

    private final DetectionRecordRepository recordRepository;

    public AnalysisServiceImpl(DetectionRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    public AnalyzeResponse analyze(String url) {
        String target = url == null ? "" : url.trim();
        Map<String, Object> features = extractFeatures(target);
        List<String> logs = new ArrayList<>();

        int score = computeRiskScore(features, logs);
        String level = score >= 75 ? "high" : (score >= 45 ? "medium" : "low");

        Map<String, Double> probs = predictProbabilities(features, score);
        Map<String, Integer> radar = buildRadar(features, probs, score);
        List<String> advice = generateAdvice(level, probs);

        DetectionRecord record = new DetectionRecord();
        record.setUrl(target);
        record.setScore(score);
        record.setLevel(level);
        record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);

        AnalyzeResponse response = new AnalyzeResponse();
        response.setScore(score);
        response.setLevel(level);
        response.setFeatures(features);
        response.setProbabilities(probs);
        response.setRadar(radar);
        response.setLogs(logs);
        response.setAdvice(advice);
        return response;
    }

    private Map<String, Object> extractFeatures(String url) {
        Map<String, Object> featureMap = new LinkedHashMap<>();
        int len = url.length();
        boolean containsIp = IP_PATTERN.matcher(url).matches();
        long specialChars = url.chars().filter(ch -> "@-_=%&".indexOf(ch) >= 0).count();
        int subdomainCount = countSubdomains(url);
        boolean https = url.startsWith("https://");
        long keywordCount = SUSPICIOUS_KEYWORDS.stream().filter(url.toLowerCase()::contains).count();
        long digits = url.chars().filter(Character::isDigit).count();
        double digitRatio = len == 0 ? 0.0 : (double) digits / len;

        featureMap.put("urlLength", len);
        featureMap.put("containsIp", containsIp ? "是" : "否");
        featureMap.put("specialChars", specialChars);
        featureMap.put("subdomainCount", subdomainCount);
        featureMap.put("https", https ? "是" : "否");
        featureMap.put("suspiciousKeywordCount", keywordCount);
        featureMap.put("digitRatio", String.format("%.2f", digitRatio));

        return featureMap;
    }

    private int computeRiskScore(Map<String, Object> features, List<String> logs) {
        int score = 0;
        int urlLength = (int) features.get("urlLength");
        boolean containsIp = "是".equals(features.get("containsIp"));
        long specialChars = (long) features.get("specialChars");
        int subdomainCount = (int) features.get("subdomainCount");
        boolean https = "是".equals(features.get("https"));
        long keywordCount = (long) features.get("suspiciousKeywordCount");
        double digitRatio = Double.parseDouble(features.get("digitRatio").toString());

        if (urlLength > 80) { score += 18; logs.add("URL过长（>80）→ 风险+18"); }
        else if (urlLength > 45) { score += 10; logs.add("URL较长（46-80）→ 风险+10"); }

        if (containsIp) { score += 20; logs.add("检测到IP地址替代域名 → 风险+20"); }
        if (specialChars >= 6) { score += 15; logs.add("特殊字符过多（>=6）→ 风险+15"); }
        else if (specialChars >= 3) { score += 8; logs.add("特殊字符偏多（3-5）→ 风险+8"); }

        if (subdomainCount >= 3) { score += 12; logs.add("子域名层级深（>=3）→ 风险+12"); }
        if (!https) { score += 14; logs.add("未使用HTTPS加密 → 风险+14"); }

        if (keywordCount >= 2) { score += 20; logs.add("命中多个敏感关键词 → 风险+20"); }
        else if (keywordCount == 1) { score += 10; logs.add("命中敏感关键词 → 风险+10"); }

        if (digitRatio > 0.2) { score += 6; logs.add("URL数字占比偏高 → 风险+6"); }

        return Math.min(100, score);
    }

    private Map<String, Double> predictProbabilities(Map<String, Object> features, int score) {
        double keywordCount = ((long) features.get("suspiciousKeywordCount"));
        boolean https = "是".equals(features.get("https"));
        boolean containsIp = "是".equals(features.get("containsIp"));
        int subdomains = (int) features.get("subdomainCount");

        // 规则+Sigmoid，模拟轻量逻辑回归效果
        double phishingLinear = -2.2 + 0.055 * score + 0.45 * keywordCount + (containsIp ? 0.7 : 0) + (https ? -0.2 : 0.25);
        double malwareLinear = -2.0 + 0.05 * score + 0.35 * subdomains + (https ? -0.15 : 0.2);
        double leakLinear = -2.1 + 0.048 * score + 0.5 * keywordCount;

        Map<String, Double> probs = new LinkedHashMap<>();
        probs.put("phishing", sigmoid(phishingLinear));
        probs.put("malware", sigmoid(malwareLinear));
        probs.put("leak", sigmoid(leakLinear));
        return probs;
    }

    private Map<String, Integer> buildRadar(Map<String, Object> features, Map<String, Double> probs, int score) {
        int xss = Math.min(100, score / 2 + ((long) features.get("specialChars") >= 4 ? 12 : 0));
        int csrf = Math.min(100, score / 2 + ("是".equals(features.get("https")) ? 10 : 20));
        int domainFraud = Math.min(100, score / 2 + ((int) features.get("subdomainCount") * 8));
        int privacyTracking = Math.min(100, score / 3 + ((long) features.get("suspiciousKeywordCount") * 15 > 100 ? 100 : (int)((long) features.get("suspiciousKeywordCount") * 15)));

        Map<String, Integer> radar = new LinkedHashMap<>();
        radar.put("phishing", (int) Math.round(probs.get("phishing") * 100));
        radar.put("malware", (int) Math.round(probs.get("malware") * 100));
        radar.put("leak", (int) Math.round(probs.get("leak") * 100));
        radar.put("xss", xss);
        radar.put("csrf", csrf);
        radar.put("domainFraud", domainFraud);
        radar.put("privacyTracking", privacyTracking);
        return radar;
    }

    private List<String> generateAdvice(String level, Map<String, Double> probs) {
        List<String> advice = new ArrayList<>();
        if ("high".equals(level)) {
            advice.add("立即关闭该页面，避免进一步交互。");
            advice.add("禁止输入账号密码、短信验证码、银行卡信息。");
            advice.add("在常用平台立即修改密码并启用双重验证（2FA）。");
        } else if ("medium".equals(level)) {
            advice.add("谨慎访问，优先手动输入官方网站地址进行比对。");
            advice.add("检查证书、域名拼写和页面内容是否异常。");
            advice.add("开启浏览器反钓鱼与下载防护功能。");
        } else {
            advice.add("当前风险较低，但仍建议保持安全浏览习惯。");
            advice.add("避免在不必要的页面填写敏感信息。");
        }

        if (probs.get("malware") > 0.6) {
            advice.add("建议使用杀毒软件进行快速扫描，避免下载可疑文件。");
        }
        return advice;
    }

    private int countSubdomains(String url) {
        try {
            String host = new URI(url).getHost();
            if (host == null) return 0;
            String[] parts = host.split("\\.");
            return Math.max(0, parts.length - 2);
        } catch (Exception e) {
            return 0;
        }
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
