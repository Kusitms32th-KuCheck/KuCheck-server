package onku.backend.domain.notice.service

import jakarta.transaction.Transactional
import onku.backend.domain.notice.NoticeCategory
import org.springframework.stereotype.Service
import onku.backend.domain.notice.dto.category.*
import onku.backend.domain.notice.enums.NoticeCategoryColor
import onku.backend.domain.notice.NoticeErrorCode
import onku.backend.domain.notice.repository.NoticeCategoryRepository
import onku.backend.global.exception.CustomException

@Service
class NoticeCategoryService(
    private val categoryRepository: NoticeCategoryRepository
) {
    @Transactional
    fun create(req: NoticeCategoryCreateRequest): NoticeCategoryResponse {
        if (categoryRepository.existsByName(req.name)) {
            throw CustomException(NoticeErrorCode.CATEGORY_NAME_DUPLICATE)
        }
        if (categoryRepository.existsByColor(req.color)) {
            throw CustomException(NoticeErrorCode.CATEGORY_COLOR_DUPLICATE)
        }

        val saved = categoryRepository.save(
            NoticeCategory(
                name = req.name.trim(),
                color = req.color
            )
        )
        return NoticeCategoryResponse.from(saved)
    }

    @Transactional
    fun update(categoryId: Long, req: NoticeCategoryUpdateRequest): NoticeCategoryResponse {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { CustomException(NoticeErrorCode.CATEGORY_NOT_FOUND) }

        // 이름이 변경되면 중복 체크
        if (category.name != req.name && categoryRepository.existsByName(req.name)) {
            throw CustomException(NoticeErrorCode.CATEGORY_NAME_DUPLICATE)
        }
        // 색상이 변경되면 중복 체크
        if (category.color != req.color && categoryRepository.existsByColor(req.color)) {
            throw CustomException(NoticeErrorCode.CATEGORY_COLOR_DUPLICATE)
        }

        category.name = req.name.trim()
        category.color = req.color
        return NoticeCategoryResponse.from(category)
    }

    @Transactional
    fun delete(categoryId: Long) {
        if (!categoryRepository.existsById(categoryId)) {
            throw CustomException(NoticeErrorCode.CATEGORY_NOT_FOUND)
        }
        if (categoryRepository.isCategoryLinkedToAnyNotice(categoryId)) {
            throw CustomException(NoticeErrorCode.CATEGORY_LINKED_NOT_DELETABLE)
        }
        categoryRepository.deleteById(categoryId)
    }

    @Transactional
    fun list(): List<NoticeCategoryResponse> =
        categoryRepository.findAllOrderByIdAsc().map(NoticeCategoryResponse::from)

    @Transactional
    fun availableColors(): AvailableColorsResponse {
        val used = categoryRepository.findAll().map { it.color }.toSet()
        val all = NoticeCategoryColor.entries.toSet()
        val available = (all - used).sortedBy { it.name }
        return AvailableColorsResponse(available)
    }
}
