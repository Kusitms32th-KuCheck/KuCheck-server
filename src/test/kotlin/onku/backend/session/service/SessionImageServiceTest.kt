package onku.backend.session.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.SessionImage
import onku.backend.domain.session.repository.SessionImageRepository
import onku.backend.domain.session.service.SessionImageService
import onku.backend.global.exception.CustomException
import onku.backend.global.s3.dto.GetS3UrlDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SessionImageServiceTest {

    @MockK(relaxUnitFun = true)
    lateinit var sessionImageRepository: SessionImageRepository

    lateinit var sessionImageService: SessionImageService

    @BeforeEach
    fun setUp() {
        sessionImageService = SessionImageService(sessionImageRepository)
    }

    private fun createSessionDetail(id: Long = 1L): SessionDetail {
        // 진짜 엔티티를 써도 되고 mockk로 써도 됨. 여기선 간단히 mock 사용.
        val detail = mockk<SessionDetail>(relaxed = true)
        every { detail.id } returns id
        return detail
    }

    // uploadImages
    @Test
    fun `uploadImages - SessionDetail과 url로 SessionImage를 생성하고 저장한다`() {
        val detail = createSessionDetail(1L)

        val preSignedImages = listOf(
            GetS3UrlDto(key = "SESSION/1/A/img1.png", preSignedUrl = "https://...1", originalName = "img1.png"),
            GetS3UrlDto(key = "SESSION/1/A/img2.png", preSignedUrl = "https://...2", originalName = "img2.png"),
        )

        // saveAll이 들어온 그대로 돌려주도록 설정 (엔티티 equals 안 맞아도 문제 없게)
        every { sessionImageRepository.saveAll(any<Iterable<SessionImage>>()) } answers {
            firstArg<Iterable<SessionImage>>().toList()
        }

        // when
        val result: List<SessionImage> = sessionImageService.uploadImages(detail, preSignedImages)

        // then: repo.saveAll이 SessionImage 리스트로 호출됐는지
        verify (exactly = 1) {
            sessionImageRepository.saveAll(
                match<Iterable<SessionImage>> { images ->
                    images.count() == 2 &&
                            images.all { it.sessionDetail == detail } &&
                            images.map { it.url }.toSet() == setOf(
                        "SESSION/1/A/img1.png",
                        "SESSION/1/A/img2.png"
                    )
                }
            )
        }

        assertEquals(2, result.size)
        assertEquals("SESSION/1/A/img1.png", result[0].url)
        assertEquals("SESSION/1/A/img2.png", result[1].url)
        assertEquals(detail, result[0].sessionDetail)
    }

    // deleteImage
    @Test
    fun `deleteImage - id 기반으로 삭제 요청을 보낸다`() {
        val imageId = 10L

        // when
        sessionImageService.deleteImage(imageId)

        // then
        verify(exactly = 1) { sessionImageRepository.deleteById(imageId) }
    }

    // getById
    @Test
    fun `getById - 이미지가 존재하면 반환한다`() {
        val imageId = 5L
        val detail = createSessionDetail(1L)
        val image = SessionImage(
            id = imageId,
            sessionDetail = detail,
            url = "SESSION/1/A/img.png"
        )

        every { sessionImageRepository.findById(imageId) } returns Optional.of(image)

        val result = sessionImageService.getById(imageId)

        assertSame(image, result)
    }

    @Test
    fun `getById - 이미지가 없으면 예외를 던진다`() {
        val imageId = 999L
        every { sessionImageRepository.findById(imageId) } returns Optional.empty()

        val ex = assertThrows<CustomException> {
            sessionImageService.getById(imageId)
        }

        assertEquals(SessionErrorCode.SESSION_IMAGE_NOT_FOUND, ex.errorCode)
    }

    // findAllBySessionDetailId
    @Test
    fun `findAllBySessionDetailId - detailId로 모든 이미지를 조회한다`() {
        val detailId = 3L
        val detail = createSessionDetail(detailId)

        val images = listOf(
            SessionImage(id = 1L, sessionDetail = detail, url = "SESSION/3/A/img1.png"),
            SessionImage(id = 2L, sessionDetail = detail, url = "SESSION/3/A/img2.png"),
        )

        every { sessionImageRepository.findAllBySessionDetailId(detailId) } returns images

        val result = sessionImageService.findAllBySessionDetailId(detailId)

        assertEquals(2, result.size)
        assertEquals(images, result)
        verify(exactly = 1) { sessionImageRepository.findAllBySessionDetailId(detailId) }
    }
}