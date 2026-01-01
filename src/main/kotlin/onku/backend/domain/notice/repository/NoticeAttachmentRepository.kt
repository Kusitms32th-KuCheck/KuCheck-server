package onku.backend.domain.notice.repository

import onku.backend.domain.notice.NoticeAttachment
import org.springframework.data.jpa.repository.JpaRepository

interface NoticeAttachmentRepository : JpaRepository<NoticeAttachment, Long> {
}