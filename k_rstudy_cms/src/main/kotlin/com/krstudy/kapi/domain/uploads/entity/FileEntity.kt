package com.krstudy.kapi.domain.uploads.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import com.krstudy.kapi.domain.uploads.dto.FileStatusEnum

@Entity(name = "StoredFile") // 엔티티 이름을 다르게 지정
class FileEntity(

    @Column(nullable = false)
    val originalFileName: String,

    @Column(nullable = false)
    val storedFileName: String,  // UUID 기반 파일명

    @Column(nullable = false)
    val filePath: String,

    @Column(nullable = false)
    val fileSize: Long,

    @Column(nullable = false)
    val fileType: String,

    @Column(nullable = false)
    val contentType: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,  // Member 엔티티와의 관계 추가

    @Column
    val checksum: String? = null,  // 파일 무결성 검사용

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: FileStatusEnum = FileStatusEnum.ACTIVE
) : BaseEntity() {
    // URL을 생성하는 프로퍼티 추가
    val url: String
        get() = "/files/$storedFileName"
}