package onku.backend.domain.session.repository

import onku.backend.domain.session.SessionDetail
import org.springframework.data.repository.CrudRepository

interface SessionDetailRepository : CrudRepository<SessionDetail, Long> {

}