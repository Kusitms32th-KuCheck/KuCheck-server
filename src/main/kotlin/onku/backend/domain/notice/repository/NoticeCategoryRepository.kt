package onku.backend.domain.notice.repository

import onku.backend.domain.notice.NoticeCategory
import onku.backend.domain.notice.enums.NoticeCategoryColor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NoticeCategoryRepository : JpaRepository<NoticeCategory, Long> {
    fun existsByName(name: String): Boolean
    fun existsByColor(color: NoticeCategoryColor): Boolean

    @Query(
        """
        select case when count(nc) > 0 then true else false end
        from Notice n
        join n.categories nc
        where nc.id = :categoryId
        """
    )
    fun isCategoryLinkedToAnyNotice(categoryId: Long): Boolean

    @Query("select nc from NoticeCategory nc order by nc.id asc")
    fun findAllOrderByIdAsc(): List<NoticeCategory>
}