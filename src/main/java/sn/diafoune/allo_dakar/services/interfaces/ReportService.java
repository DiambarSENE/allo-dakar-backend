package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.web.dtos.report.CreateReportRequest;
import sn.diafoune.allo_dakar.web.dtos.report.ReportResponse;

import java.util.UUID;

public interface ReportService {

    ReportResponse create(UUID reporterId, CreateReportRequest request);

    Page<ReportResponse> listMine(UUID reporterId, Pageable pageable);

    Page<ReportResponse> listAll(Pageable pageable);

    ReportResponse handle(UUID reportId, UUID handledByAdminId, String resolution, boolean resolved);
}
