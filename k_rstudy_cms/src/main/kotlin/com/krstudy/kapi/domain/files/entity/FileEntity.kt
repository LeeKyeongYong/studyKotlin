package com.krstudy.kapi.domain.files.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class FileEntity (
    @Column(length = 255)
    val originalFilename: String, // 원래 파일 이름

    @Column(length = 255)
    val storedFilename: String, // 저장된 파일 이름 (경로 포함)

    val relatedEntityId: Long, // 다양한 엔티티와의 관계를 나타내기 위한 ID
    val entityType: String // 어떤 엔티티에 해당하는지 구분하기 위한 타입 (예: "EMAIL", "BOARD" 등)
) : BaseEntity() {}