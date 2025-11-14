package onku.backend.domain.notice.repository

import onku.backend.domain.notice.Notice
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface NoticeRepository : JpaRepository<Notice, Long> {

    @EntityGraph(attributePaths = ["categories", "attachments"])
    fun findAllByOrderByPublishedAtDescIdDesc(pageable: Pageable): Page<Notice>

    @EntityGraph(attributePaths = ["categories", "attachments"])
    fun findDistinctByCategories_IdOrderByPublishedAtDescIdDesc(
        categoryId: Long,
        pageable: Pageable
    ): Page<Notice>
}