package onku.backend.domain.notice

import jakarta.persistence.*
import onku.backend.global.s3.enums.UploadOption

@Entity
@Table(name = "notice_attachment")
class NoticeAttachment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_attachment_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "notice_id", nullable = true)
    var notice: Notice? = null, // 공지 생성 전에도 이미지 업로드를 허용하기 위해 nullable

    @Column(name = "s3_key", nullable = false)
    var s3Key: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false)
    val attachmentType: UploadOption,

    @Column(name = "attachment_size")
    val attachmentSize: Long? = null, // 파일 크기
)