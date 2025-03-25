package com.krstudy.kapi.domain.popups.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "popup_statistics")
class PopupStatisticsEntity(


    @Column(nullable = false)
    val popupId: Long,

    @Column(nullable = false)
    var viewCount: Long = 0,

    @Column(nullable = false)
    var clickCount: Long = 0,

    @Column(nullable = false)
    var closeCount: Long = 0,

    @ElementCollection
    @CollectionTable(name = "popup_device_stats")
    @MapKeyColumn(name = "device_type")
    @Column(name = "count")
    var deviceStats: MutableMap<String, Long> = mutableMapOf(
        "DESKTOP" to 0,
        "MOBILE" to 0,
        "TABLET" to 0
    ),

    @ElementCollection
    @CollectionTable(name = "popup_close_type_stats")
    @MapKeyColumn(name = "close_type")
    @Column(name = "count")
    var closeTypeStats: MutableMap<String, Long> = mutableMapOf(
        "NORMAL" to 0,
        "AUTO" to 0,
        "TODAY" to 0
    ),

    @Column(nullable = false)
    var viewDuration: Double = 0.0,

    @Column(nullable = false)
    val hour: Int = LocalDateTime.now().hour,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val deviceType: DeviceType
) : BaseEntity()