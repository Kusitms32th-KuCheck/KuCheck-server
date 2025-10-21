package onku.backend.domain.member.repository


import onku.backend.domain.member.MemberProfile
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
}
