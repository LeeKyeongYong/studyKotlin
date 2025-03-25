package com.krstudy.kapi.domain.popups.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "popup_targetings")
class PopupTargeting(
    @ManyToMany
    @JoinTable(
        name = "popup_targeting_segments",
        joinColumns = [JoinColumn(name = "targeting_id")],
        inverseJoinColumns = [JoinColumn(name = "segment_id")]
    )
    var userSegments: MutableSet<UserSegment> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(
        name = "popup_target_regions",
        joinColumns = [JoinColumn(name = "targeting_id")]
    )
    @Column(name = "region")
    var targetRegions: MutableSet<String> = mutableSetOf(),

    @Column(name = "previous_interaction_rule")
    private var _previousInteractionRule: String? = null,

    @Column(name = "min_page_dwell_time")
    private var _minPageDwellTime: Int? = null
) : BaseEntity() {

    @OneToOne(mappedBy = "targeting")  // mappedBy를 "targeting"으로 수정
    var popup: PopupEntity? = null
        protected set

    var previousInteractionRule: String?
        get() = _previousInteractionRule
        set(value) {
            _previousInteractionRule = value
        }

    var minPageDwellTime: Int?
        get() = _minPageDwellTime
        set(value) {
            _minPageDwellTime = value
        }

    fun addUserSegment(segment: UserSegment) {
        userSegments.add(segment)
        segment.popupTargetings.add(this)
    }

    fun removeUserSegment(segment: UserSegment) {
        userSegments.remove(segment)
        segment.popupTargetings.remove(this)
    }

    fun addTargetRegion(region: String) {
        targetRegions.add(region)
    }

    fun removeTargetRegion(region: String) {
        targetRegions.remove(region)
    }

    internal fun connectPopup(popupEntity: PopupEntity?) {
        this.popup = popupEntity
    }
}