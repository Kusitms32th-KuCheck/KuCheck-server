package onku.backend.domain.absence.repository

import onku.backend.domain.absence.AbsenceReport
import org.springframework.data.jpa.repository.JpaRepository

interface AbsenceReportRepository : JpaRepository<AbsenceReport, Long> {
}