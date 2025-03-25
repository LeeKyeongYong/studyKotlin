package com.krstudy.kapi.domain.popups.entity

import com.krstudy.kapi.domain.popups.enums.RepeatType
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 팝업 반복 설정 엔티티
 */
@Entity
@Table(name = "popup_schedules")
class PopupScheduleEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id")
    var popup: PopupEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var repeatType: RepeatType,

    @Column
    var repeatDays: String? = null, // "MON,WED,FRI" 형식으로 저장

    @Column
    var repeatMonthDay: Int? = null,

    @Column(nullable = false)
    var startTime: LocalDateTime,

    @Column(nullable = false)
    var endTime: LocalDateTime,

    @Column(nullable = false)
    var isActive: Boolean = true
) : BaseEntity()