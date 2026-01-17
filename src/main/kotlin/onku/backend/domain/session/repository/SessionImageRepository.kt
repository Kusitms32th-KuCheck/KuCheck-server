package onku.backend.domain.session.repository

import onku.backend.domain.session.SessionImage
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SessionImageRepository : CrudRepository<SessionImage, Long> {
    fun findAllBySessionDetailId(sessionDetailId: Long): List<SessionImage>

    @Query("""
        SELECT si
        FROM SessionImage si
        WHERE si.sessionDetail.id = :detailId
        ORDER BY si.id ASC
    """)
    fun findByDetailId(@Param("detailId") detailId: Long): List<SessionImage>

    @Query("""
        select si.url
        from SessionImage si
        where si.sessionDetail.id = :detailId
    """)
    fun findAllImageKeysByDetailId(@Param("detailId") detailId: Long): List<String>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from SessionImage si
        where si.sessionDetail.id = :detailId
    """)
    fun deleteByDetailIdBulk(@Param("detailId") detailId: Long): Int
}