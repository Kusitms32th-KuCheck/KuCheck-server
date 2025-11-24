package onku.backend.session.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import onku.backend.domain.attendance.finalize.FinalizeEvent
import onku.backend.domain.session.Session
import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.repository.SessionDetailRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.domain.session.service.SessionDetailService
import onku.backend.global.exception.CustomException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalTime
import java.util.*
import kotlin.test.Test
@ExtendWith(MockKExtension::class)
class SessionDetailServiceTest {
    @MockK lateinit var sessionDetailRepository: SessionDetailRepository
    @MockK lateinit var sessionRepository: SessionRepository
    @MockK(relaxUnitFun = true) lateinit var applicationEventPublisher: ApplicationEventPublisher

    lateinit var service: SessionDetailService

    @BeforeEach
    fun setUp() {
        service = SessionDetailService(
            sessionDetailRepository = sessionDetailRepository,
            sessionRepository = sessionRepository,
            applicationEventPublisher = applicationEventPublisher
        )
        clearMocks(sessionDetailRepository, sessionRepository, applicationEventPublisher)

    }

    private fun createSession(id: Long = 10L): Session {
        val s = mockk<Session>(relaxed = true)
        every { s.id } returns id
        every { s.sessionDetail = any() } just Runs
        return s
    }

    // ==========================
    // 1) sessionDetailId 존재 → 기존 상세 수정
    // ==========================
    @Test
    fun `upsertSessionDetail - 기존 상세가 있으면 수정한다`() {
        val session = createSession(id = 100L)

        val existingDetail = SessionDetail(
            id = 1L,
            place = "기존 장소",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(11, 0),
            content = "기존 내용"
        )

        val request = UpsertSessionDetailRequest(
            sessionId = 10L,
            sessionDetailId = 1L,
            place = "새 장소",
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            content = "새 내용"
        )

        every { sessionDetailRepository.findById(1L) } returns Optional.of(existingDetail)
        every { sessionRepository.save(session) } returns session
        // Unit 메서드라서 just Runs (혹은 @MockK(relaxUnitFun = true)면 없어도 됨)
        every { applicationEventPublisher.publishEvent(any()) } just Runs

        // when
        val returnedId = service.upsertSessionDetail(session, request)

        // then - detail 변경 확인
        assertEquals(1L, returnedId)
        assertEquals("새 장소", existingDetail.place)
        assertEquals(LocalTime.of(10, 0), existingDetail.startTime)
        assertEquals(LocalTime.of(12, 0), existingDetail.endTime)
        assertEquals("새 내용", existingDetail.content)

        // 세션에 detail 연결 + 세션 저장
        verify { session.sessionDetail = existingDetail }
        verify { sessionRepository.save(session) }

        // FinalizeEvent 발행됐는지만 확인 (runAt까지는 굳이 안 봐도 됨)
        val eventSlot = slot<FinalizeEvent>()
        verify {
            applicationEventPublisher.publishEvent(capture(eventSlot))
        }
        assertEquals(100L, eventSlot.captured.sessionId)   // 세션 ID만 검증
        // runAt은 null 아니면 됐다 정도만 보고 싶으면:
        // assertNotNull(eventSlot.captured.runAt)
    }

    // ==========================
    // 2) sessionDetailId 없음 → 새 상세 생성
    // ==========================
    @Test
    fun `upsertSessionDetail - sessionDetailId가 없으면 새로 생성한다`() {
        val session = createSession(id = 200L)

        val request = UpsertSessionDetailRequest(
            sessionId = 10L,
            sessionDetailId = null,
            place = "강의실 101",
            startTime = LocalTime.of(13, 0),
            endTime = LocalTime.of(15, 0),
            content = "세션 내용"
        )

        val savedDetail = SessionDetail(
            id = 10L,
            place = request.place,
            startTime = request.startTime,
            endTime = request.endTime,
            content = request.content
        )

        every { sessionDetailRepository.save(any()) } returns savedDetail
        every { sessionRepository.save(session) } returns session
        every { applicationEventPublisher.publishEvent(any()) } just Runs

        // when
        val returnedId = service.upsertSessionDetail(session, request)

        // then
        assertEquals(10L, returnedId)

        // 새 detail이 기대한 값으로 save 되었는지 검증
        verify {
            sessionDetailRepository.save(
                match {
                    it.place == "강의실 101" &&
                            it.startTime == LocalTime.of(13, 0) &&
                            it.endTime == LocalTime.of(15, 0) &&
                            it.content == "세션 내용"
                }
            )
        }

        // 세션에 detail 연결 + 세션 저장
        verify { session.sessionDetail = savedDetail }
        verify { sessionRepository.save(session) }

        // FinalizeEvent 발행 확인 (sessionId만 체크, runAt은 null 아님만 확인)
        val eventSlot = slot<FinalizeEvent>()
        verify { applicationEventPublisher.publishEvent(capture(eventSlot)) }

        assertEquals(200L, eventSlot.captured.sessionId)
        assertNotNull(eventSlot.captured.runAt)
    }

    // ==========================
    // 3) sessionDetailId 있는데 DB에 없음 → 예외
    // ==========================
    @Test
    fun `upsertSessionDetail - 기존 상세 없으면 예외`() {
        val session = createSession(id = 300L)

        val request = UpsertSessionDetailRequest(
            sessionId = 10L,
            sessionDetailId = 999L,
            place = "어딘가",
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 0),
            content = "내용"
        )

        every { sessionDetailRepository.findById(999L) } returns Optional.empty()

        val ex = assertThrows<CustomException> {
            service.upsertSessionDetail(session, request)
        }

        assertEquals(SessionErrorCode.SESSION_DETAIL_NOT_FOUND, ex.errorCode)
        verify(exactly = 0) { sessionRepository.save(any()) }
        verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
    }

    // ==========================
    // 4) getById - 정상
    // ==========================
    @Test
    fun `getById - 정상 조회`() {
        val detail = SessionDetail(
            id = 5L,
            place = "강의실",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(11, 0),
            content = "내용"
        )

        every { sessionDetailRepository.findById(5L) } returns Optional.of(detail)

        val found = service.getById(5L)

        assertEquals(5L, found.id)
        assertEquals("강의실", found.place)
    }

    // ==========================
    // 5) getById - 못 찾으면 예외
    // ==========================
    @Test
    fun `getById - 상세 없으면 예외`() {
        every { sessionDetailRepository.findById(100L) } returns Optional.empty()

        val ex = assertThrows<CustomException> {
            service.getById(100L)
        }

        assertEquals(SessionErrorCode.SESSION_DETAIL_NOT_FOUND, ex.errorCode)
    }
}