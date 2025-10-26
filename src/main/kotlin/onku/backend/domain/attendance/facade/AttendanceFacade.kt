package onku.backend.domain.attendance.facade

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import onku.backend.domain.attendance.dto.AttendanceTokenResponse
import onku.backend.domain.attendance.service.AttendanceService
import onku.backend.domain.member.Member
import onku.backend.domain.member.service.MemberProfileService

@Component
class AttendanceFacade(
    private val attendanceService: AttendanceService,
    private val memberProfileService: MemberProfileService
) {
    @Transactional(readOnly = true)
    fun issueTokenWithProfile(member: Member): AttendanceTokenResponse {
        val core = attendanceService.issueAttendanceTokenFor(member)
        val basics = memberProfileService.getProfileBasics(member)

        return AttendanceTokenResponse(
            token = core.token,
            expAt = core.expAt,
            name = basics.name,
            part = basics.part,
            school = basics.school,
            profileImageUrl = basics.profileImageUrl
        )
    }
}
