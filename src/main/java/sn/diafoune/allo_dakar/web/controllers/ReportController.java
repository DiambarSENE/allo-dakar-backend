package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.PaginationUtils;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.services.interfaces.ReportService;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.report.CreateReportRequest;
import sn.diafoune.allo_dakar.web.dtos.report.ReportResponse;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> create(@Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Signalement enregistré", response));
    }

    @GetMapping("/me")
    public PagedResponse<ReportResponse> mine(@RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Signalements récupérés",
                reportService.listMine(SecurityUtils.currentUserId(), PaginationUtils.of(page, size, null, null)));
    }
}
