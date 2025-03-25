package com.krstudy.kapi.domain.popups.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.popups.dto.PopupResponse
import com.krstudy.kapi.domain.popups.enums.PopupStatus
import com.krstudy.kapi.domain.uploads.entity.FileEntity
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "Popup")
class PopupEntity(
    @Column(nullable = false, length = 100)
    var title: String,

    @Column(nullable = false, length = 2000)
    var content: String,

    @Column(nullable = false)
    var startDateTime: LocalDateTime,

    @Column(nullable = false)
    var endDateTime: LocalDateTime,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PopupStatus = PopupStatus.ACTIVE,

    @Column(nullable = false)
    var priority: Int,

    @Column(nullable = false)
    var width: Int,

    @Column(nullable = false)
    var height: Int,

    @Column(nullable = false)
    var positionX: Int,

    @Column(nullable = false)
    var positionY: Int,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    var image: FileEntity?,

    @Column
    var linkUrl: String?,

    @Column
    var altText: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var target: PopupTarget = PopupTarget.SELF,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var deviceType: DeviceType = DeviceType.ALL,

    @Column(nullable = false)
    var cookieExpireDays: Int = 1,

    @Column(nullable = false)
    var hideForToday: Boolean = true,

    @Column(nullable = false)
    var hideForWeek: Boolean = false,

    @Column
    var backgroundColor: String?,

    @Column
    var borderStyle: String?,

    @Column(nullable = false)
    var shadowEffect: Boolean = false,

    @Column
    @Enumerated(EnumType.STRING)
    var animationType: AnimationType?,

    @ElementCollection
    @CollectionTable(name = "popup_display_pages")
    var displayPages: Set<String> = setOf(),

    @ElementCollection
    @CollectionTable(name = "popup_target_roles")
    var targetRoles: Set<String> = setOf(),

    @Column(nullable = false)
    var maxDisplayCount: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    var creator: Member,

    @Column(nullable = false)
    var viewCount: Long = 0,

    @Column(nullable = false)
    var clickCount: Long = 0,

    @OneToMany(mappedBy = "popup", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val histories: MutableList<PopupHistoryEntity> = mutableListOf()


    ) : BaseEntity() {

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "targeting_id")
    var targeting: PopupTargeting? = null
        set(value) {
            field = value
            value?.connectPopup(this)
        }

    fun copy(
        title: String = this.title,
        status: PopupStatus = this.status,
        creator: Member = this.creator,
        viewCount: Long = this.viewCount,
        clickCount: Long = this.clickCount
    ): PopupEntity {
        return PopupEntity(
            title = "$title (복사본)",
            content = this.content,
            startDateTime = this.startDateTime,
            endDateTime = this.endDateTime,
            status = status,
            priority = this.priority,
            width = this.width,
            height = this.height,
            positionX = this.positionX,
            positionY = this.positionY,
            image = null,
            linkUrl = this.linkUrl,
            altText = this.altText,
            target = this.target,
            deviceType = this.deviceType,
            cookieExpireDays = this.cookieExpireDays,
            hideForToday = this.hideForToday,
            hideForWeek = this.hideForWeek,
            backgroundColor = this.backgroundColor,
            borderStyle = this.borderStyle,
            shadowEffect = this.shadowEffect,
            animationType = this.animationType,
            displayPages = this.displayPages.toSet(),  // 새로운 Set 인스턴스 생성
            targetRoles = this.targetRoles.toSet(),    // 새로운 Set 인스턴스 생성
            maxDisplayCount = this.maxDisplayCount,
            creator = creator,
            viewCount = viewCount,
            clickCount = clickCount
        ).also {
            it.targeting = null
        }

    }

    fun toResponse(): PopupResponse {
        return PopupResponse(
            id = this.id,
            title = this.title,
            content = this.content,
            startDateTime = this.startDateTime,
            endDateTime = this.endDateTime,
            status = this.status,
            priority = this.priority,
            width = this.width,
            height = this.height,
            positionX = this.positionX,
            positionY = this.positionY,
            imageUrl = this.image?.let { "/api/v1/files/view/${it.id}" },
            linkUrl = this.linkUrl,
            altText = this.altText,
            target = this.target.name,
            deviceType = this.deviceType.name,
            viewCount = this.viewCount,
            clickCount = this.clickCount,
            hideForToday = this.hideForToday,
            hideForWeek = this.hideForWeek,
            createDate = this.getCreateDate() ?: LocalDateTime.now()
        )
    }

    fun incrementViewCount() {
        this.viewCount++
    }

    fun incrementClickCount() {
        this.clickCount++
    }

    fun isActive(): Boolean {
        val now = LocalDateTime.now()
        return status == PopupStatus.ACTIVE &&
                now.isAfter(startDateTime) &&
                now.isBefore(endDateTime)
    }

    fun changeStatus(newStatus: PopupStatus) {
        this.status = newStatus
    }

    fun update(
        title: String? = null,
        content: String? = null,
        startDateTime: LocalDateTime? = null,
        endDateTime: LocalDateTime? = null,
        priority: Int? = null,
        width: Int? = null,
        height: Int? = null,
        positionX: Int? = null,
        positionY: Int? = null,
        image: FileEntity? = null,
        linkUrl: String? = null,
        altText: String? = null
    ) {
        title?.let { this.title = it }
        content?.let { this.content = it }
        startDateTime?.let { this.startDateTime = it }
        endDateTime?.let { this.endDateTime = it }
        priority?.let { this.priority = it }
        width?.let { this.width = it }
        height?.let { this.height = it }
        positionX?.let { this.positionX = it }
        positionY?.let { this.positionY = it }
        image?.let { this.image = it }
        linkUrl?.let { this.linkUrl = it }
        altText?.let { this.altText = it }
    }
}