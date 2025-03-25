package com.krstudy.kapi.domain.popups.dto

import com.krstudy.kapi.domain.popups.entity.AnimationType
import com.krstudy.kapi.domain.popups.entity.DeviceType
import com.krstudy.kapi.domain.popups.entity.PopupTarget
import java.time.LocalDateTime

data class PopupCreateRequest(
    val title: String,
    val content: String,
    val startDateTime: LocalDateTime,  // String에서 LocalDateTime으로 변경
    val endDateTime: LocalDateTime,    // String에서 LocalDateTime으로 변경
    val priority: Int,
    val width: Int,
    val height: Int,
    val positionX: Int,
    val positionY: Int,
    val linkUrl: String?,
    val altText: String?,
    val target: PopupTarget,          // String에서 PopupTarget으로 변경
    val deviceType: DeviceType,       // String에서 DeviceType으로 변경
    val cookieExpireDays: Int,
    val hideForToday: Boolean,
    val hideForWeek: Boolean,
    val backgroundColor: String?,
    val borderStyle: String?,
    val shadowEffect: Boolean,
    val animationType: AnimationType?, // String에서 AnimationType으로 변경
    val displayPages: Set<String>,
    val targetRoles: Set<String>,     // targetUserGroups에서 targetRoles로 변경
    val maxDisplayCount: Int
)