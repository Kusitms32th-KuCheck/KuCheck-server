package onku.backend.domain.notice.repository

import onku.backend.domain.notice.NoticeImage
import org.springframework.data.jpa.repository.JpaRepository

interface NoticeImageRepository : JpaRepository<NoticeImage, Long>
