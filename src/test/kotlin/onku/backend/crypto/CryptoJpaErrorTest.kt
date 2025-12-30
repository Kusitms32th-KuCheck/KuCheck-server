package onku.backend.crypto


import onku.backend.domain.absence.enums.AbsenceApprovedType
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.global.crypto.exception.DecryptionException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CryptoJpaErrorTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val memberRepository: MemberRepository,
) {

    @Test
    fun `jpa decrypt error is wrapped as JpaSystemException`() {
        // ✅ 1) DB에 "깨진 암호문"을 직접 넣기 (Base64 아님)
        jdbcTemplate.update(
            "insert into MEMBER (member_id, email, role, social_type, social_id, has_info, approval, is_tf, is_staff, created_at, updated_at) values (?,?,?,?,?,?,?,?,?,?,?)",
            1L,
            "abc@def.com",
            Role.USER.name,
            SocialType.KAKAO.name,
            1234567890,
            false,
            ApprovalStatus.APPROVED.name,
            false,
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        )
        jdbcTemplate.update(
            "insert into MEMBER_PROFILE (member_id, name) values (?, ?)",
            1L,
            "%%%not-base64%%%"
        )

        // ✅ 2) 조회 시 Converter decrypt 실행 -> 예외 -> JPA 래핑
        val ex = assertThrows(JpaSystemException::class.java) {
            memberRepository.findById(1L).orElseThrow()
        }

        // ✅ 3) 원인 체인에 DecryptionException이 있는지 확인
        assertTrue(ex.hasCause(DecryptionException::class.java))
    }

    private fun Throwable.hasCause(type: Class<*>): Boolean =
        generateSequence(this) { it.cause }.any { type.isInstance(it) }
}