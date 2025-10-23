package onku.backend.domain.session.repository

import onku.backend.domain.session.SessionImage
import org.springframework.data.repository.CrudRepository

interface SessionImageRepository : CrudRepository<SessionImage, Long> {
    fun findAllBySessionDetailId(sessionDetailId: Long): List<SessionImage>
}