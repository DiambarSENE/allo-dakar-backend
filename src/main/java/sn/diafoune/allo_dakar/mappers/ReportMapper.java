package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Report;
import sn.diafoune.allo_dakar.web.dtos.report.ReportResponse;

@Component
public class ReportMapper {

    public ReportResponse toResponse(Report report) {
        if (report == null) {
            return null;
        }
        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReportedUser() != null ? report.getReportedUser().getId() : null,
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getResolution(),
                report.getResolvedAt()
        );
    }
}
