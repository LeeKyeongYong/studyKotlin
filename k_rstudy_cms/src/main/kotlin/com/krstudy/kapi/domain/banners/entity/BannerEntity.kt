package com.krstudy.kapi.domain.banners.entity

import com.krstudy.kapi.domain.banners.enums.BannerStatus
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.uploads.entity.FileEntity
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "Banner")
class BannerEntity(
    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var description: String,

    @Column
    var linkUrl: String? = null,

    @Column(nullable = false)
    var displayOrder: Int,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: BannerStatus = BannerStatus.ACTIVE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    var bannerImage: FileEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var creator: Member,

    @Column(nullable = false)
    var startDate: LocalDateTime,

    @Column(nullable = false)
    var endDate: LocalDateTime
) : BaseEntity()