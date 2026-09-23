package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.Report;
import sn.diafoune.allo_dakar.entities.enums.ReportStatus;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    Page<Report> findByReporterId(UUID reporterId, Pageable pageable);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
}
