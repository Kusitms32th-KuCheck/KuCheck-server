package onku.backend.domain.member.repository

import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.enums.ApprovalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph

interface MemberProfileRepository : JpaRepository<MemberProfile, Long> {
    fun existsByMember_Id(memberId: Long): Boolean

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from MemberProfile mp where mp.member.id = :memberId")
    fun deleteByMemberId(@Param("memberId") memberId: Long): Int

    @EntityGraph(attributePaths = ["member"])
    fun findAllByOrderByPartAscNameAsc(pageable: Pageable): Page<MemberProfile>

    // PENDING, REJECTED
    @EntityGraph(attributePaths = ["member"])
    fun findByMember_ApprovalIn(
        approvals: Collection<ApprovalStatus>,
        pageable: Pageable
    ): Page<MemberProfile>

    // APPROVED
    @EntityGraph(attributePaths = ["member"])
    fun findByMemberApproval(
        approval: ApprovalStatus,
        pageable: Pageable
    ): Page<MemberProfile>

    // STAFF
    @EntityGraph(attributePaths = ["member"])
    fun findByMemberApprovalAndMemberIsStaff(
        approval: ApprovalStatus,
        isStaff: Boolean,
        pageable: Pageable
    ): Page<MemberProfile>
}
