// domain/popups/factory/PopupFactory.kt
package com.krstudy.kapi.domain.popups.factory

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.popups.dto.PopupCreateRequest
import com.krstudy.kapi.domain.popups.entity.PopupEntity
import com.krstudy.kapi.domain.popups.enums.PopupStatus
import com.krstudy.kapi.domain.uploads.entity.FileEntity
import org.springframework.stereotype.Component

@Component
class PopupFactory {
    fun createPopupEntity(
        request: PopupCreateRequest,
        image: FileEntity?,
        creator: Member
    ): PopupEntity {
        return PopupEntity(
            title = request.title,
            content = request.content,
            startDateTime = request.startDateTime,
            endDateTime = request.endDateTime,
            status = PopupStatus.ACTIVE,
            priority = request.priority,
            width = request.width,
            height = request.height,
            positionX = request.positionX,
            positionY = request.positionY,
            image = image,
            linkUrl = request.linkUrl,
            altText = request.altText,
            target = request.target,
            deviceType = request.deviceType,
            cookieExpireDays = request.cookieExpireDays,
            hideForToday = request.hideForToday,
            hideForWeek = request.hideForWeek,
            backgroundColor = request.backgroundColor,
            borderStyle = request.borderStyle,
            shadowEffect = request.shadowEffect,
            animationType = request.animationType,
            displayPages = request.displayPages.toSet(),
            targetRoles = request.targetRoles.toSet(),
            maxDisplayCount = request.maxDisplayCount,
            creator = creator
        )
    }
}