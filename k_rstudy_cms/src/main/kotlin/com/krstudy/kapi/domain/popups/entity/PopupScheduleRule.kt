package com.krstudy.kapi.domain.popups.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "popup_schedule_rules")
class PopupScheduleRule(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id")
    var popup: PopupEntity,

    @ElementCollection
    @CollectionTable(
        name = "popup_schedule_days",
        joinColumns = [JoinColumn(name = "schedule_rule_id")]
    )
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    var days: MutableSet<DayOfWeek> = mutableSetOf(),

    @Column(name = "start_time")
    var startTime: LocalTime,

    @Column(name = "end_time")
    var endTime: LocalTime,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "priority")
    var priority: Int = 0
) : BaseEntity() {

    fun isActiveForTime(time: LocalTime): Boolean {
        return isActive && time in startTime..endTime
    }

    fun isActiveForDay(day: DayOfWeek): Boolean {
        return isActive && days.contains(day)
    }
}