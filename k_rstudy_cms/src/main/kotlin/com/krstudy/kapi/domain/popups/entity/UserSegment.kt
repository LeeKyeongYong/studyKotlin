package com.krstudy.kapi.domain.popups.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "user_segments")
class UserSegment(
    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var criteria: String, // JSON 형태로 저장된 세그먼트 기준

    @Column(nullable = false)
    var isActive: Boolean = true,

    @ManyToMany(mappedBy = "userSegments")
    var popupTargetings: MutableSet<PopupTargeting> = mutableSetOf()
) : BaseEntity() {
    // 연관관계 편의 메서드
    fun addPopupTargeting(targeting: PopupTargeting) {
        popupTargetings.add(targeting)
        targeting.userSegments.add(this)
    }

    fun removePopupTargeting(targeting: PopupTargeting) {
        popupTargetings.remove(targeting)
        targeting.userSegments.remove(this)
    }
}