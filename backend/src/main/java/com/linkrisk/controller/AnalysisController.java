package com.linkrisk.controller;

import com.linkrisk.dto.AnalyzeRequest;
import com.linkrisk.dto.AnalyzeResponse;
import com.linkrisk.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestBody @Valid AnalyzeRequest request) {
        return analysisService.analyze(request.getUrl());
    }
}
