package onku.backend.domain.session.facade

import onku.backend.domain.session.dto.SessionAboutAbsenceResponse
import onku.backend.domain.session.dto.SessionSaveRequest
import onku.backend.domain.session.service.SessionService
import onku.backend.global.page.PageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class SessionFacade(
    private val sessionService: SessionService
) {
    fun showSessionAboutAbsence(page: Int, size: Int): PageResponse<SessionAboutAbsenceResponse> {
        val pageRequest = PageRequest.of(page, size)
        val sessionPage = sessionService.getUpcomingSessionsForAbsence(pageRequest)
        return PageResponse.from(sessionPage)
    }

    fun sessionSave(sessionSaveRequestList: List<SessionSaveRequest>): Boolean {
        return sessionService.saveAll(sessionSaveRequestList)
    }
}