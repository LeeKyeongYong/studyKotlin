package com.krstudy.kapi.domain.popups.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 팝업 수정 이력 엔티티
 */
@Entity
@Table(name = "popup_history")
class PopupHistoryEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id")
    val popup: PopupEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id")
    val editor: Member,

    @Column(nullable = false)
    val action: String,

    @Column(columnDefinition = "TEXT")
    val changeDetails: String?, // String 타입으로 변경

    @Column(nullable = false)
    val actionDate: LocalDateTime = LocalDateTime.now()
) : BaseEntity()