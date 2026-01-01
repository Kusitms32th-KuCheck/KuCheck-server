package onku.backend.session.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import onku.backend.domain.session.Session
import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.SessionImage
import onku.backend.domain.session.repository.SessionImageRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.domain.session.service.SessionNoticeService
import onku.backend.global.exception.CustomException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SessionNoticeServiceTest {

    @MockK lateinit var sessionRepository: SessionRepository
    @MockK lateinit var sessionImageRepository: SessionImageRepository

    lateinit var sessionNoticeService: SessionNoticeService

    @BeforeEach
    fun setUp() {
        sessionNoticeService = SessionNoticeService(
            sessionRepository = sessionRepository,
            sessionImageRepository = sessionImageRepository
        )
    }

    private fun createSession(
        id: Long = 1L,
        detail: SessionDetail? = null
    ): Session {
        val s = mockk<Session>(relaxed = true)
        every { s.id } returns id
        every { s.sessionDetail } returns detail
        return s
    }

    private fun createDetail(
        id: Long = 10L
    ): SessionDetail {
        val d = mockk<SessionDetail>(relaxed = true)
        every { d.id } returns id
        return d
    }

    // 정상 케이스
    @Test
    fun `getSessionWithImages - 세션과 디테일, 이미지가 있으면 Triple을 반환한다`() {
        val sessionId = 1L
        val detailId = 10L

        val detail = createDetail(detailId)
        val session = createSession(id = sessionId, detail = detail)

        val images = listOf(
            SessionImage(id = 100L, sessionDetail = detail, url = "SESSION/1/A/img1.png"),
            SessionImage(id = 101L, sessionDetail = detail, url = "SESSION/1/A/img2.png"),
        )

        every { sessionRepository.findWithDetail(sessionId) } returns session
        every { sessionImageRepository.findByDetailId(detailId) } returns images

        // when
        val (returnedSession, returnedDetail, returnedImages) =
            sessionNoticeService.getSessionWithImages(sessionId)

        // then
        assertSame(session, returnedSession)
        assertSame(detail, returnedDetail)
        assertEquals(2, returnedImages.size)
        assertEquals(images, returnedImages)

        verify(exactly = 1) { sessionRepository.findWithDetail(sessionId) }
        verify(exactly = 1) { sessionImageRepository.findByDetailId(detailId) }
    }

    // 세션이 없을 때
    @Test
    fun `getSessionWithImages - 세션이 없으면 SESSION_NOT_FOUND 예외`() {
        val sessionId = 99L

        every { sessionRepository.findWithDetail(sessionId) } returns null

        val ex = assertThrows<CustomException> {
            sessionNoticeService.getSessionWithImages(sessionId)
        }

        assertEquals(SessionErrorCode.SESSION_NOT_FOUND, ex.errorCode)
        verify(exactly = 1) { sessionRepository.findWithDetail(sessionId) }
        verify(exactly = 0) { sessionImageRepository.findByDetailId(any()) }
    }

    // 디테일이 없을 때
    @Test
    fun `getSessionWithImages - sessionDetail이 없으면 SESSION_DETAIL_NOT_FOUND 예외`() {
        val sessionId = 1L
        val session = createSession(id = sessionId, detail = null)

        every { sessionRepository.findWithDetail(sessionId) } returns session

        val ex = assertThrows<CustomException> {
            sessionNoticeService.getSessionWithImages(sessionId)
        }

        assertEquals(SessionErrorCode.SESSION_DETAIL_NOT_FOUND, ex.errorCode)
        verify(exactly = 1) { sessionRepository.findWithDetail(sessionId) }
        verify(exactly = 0) { sessionImageRepository.findByDetailId(any()) }
    }
}