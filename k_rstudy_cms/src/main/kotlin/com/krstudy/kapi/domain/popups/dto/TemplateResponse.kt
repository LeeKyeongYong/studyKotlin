package com.krstudy.kapi.domain.popups.dto

import com.krstudy.kapi.domain.popups.entity.PopupTemplateEntity
import java.time.LocalDateTime

data class TemplateResponse(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val backgroundColor: String?,
    val borderStyle: String?,
    val content: String?,
    val isDefault: Boolean,
    val createDate: LocalDateTime
) {
    companion object {
        fun from(template: PopupTemplateEntity): TemplateResponse {
            return TemplateResponse(
                id = template.id,
                name = template.name,
                content = template.content,
                width = template.width,
                height = template.height,
                backgroundColor = template.backgroundColor,
                borderStyle = template.borderStyle,
                isDefault = template.isDefault,
                createDate = template.getCreateDate() ?: LocalDateTime.now()
            )
        }
    }
}