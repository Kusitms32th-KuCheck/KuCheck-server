package onku.backend.domain.notice.repository

import onku.backend.domain.notice.Notice
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface NoticeRepository : JpaRepository<Notice, Long> {

    @EntityGraph(attributePaths = ["categories", "files"])
    fun findAllByOrderByPublishedAtDescIdDesc(): List<Notice>
}