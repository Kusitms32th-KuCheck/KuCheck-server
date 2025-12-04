package onku.backend.domain.kupick.repository.projection

import onku.backend.domain.kupick.Kupick
import onku.backend.domain.member.MemberProfile

interface KupickWithProfile {
    val kupick: Kupick
    val memberProfile: MemberProfile
}