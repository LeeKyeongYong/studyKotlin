package com.krstudy.kapi.domain.popups.dto

import com.krstudy.kapi.domain.popups.entity.PopupEntity

data class PopupPreviewResponse(
    val id: Long,
    val title: String,
    val content: String,
    val width: Int,
    val height: Int,
    val imageUrl: String?,
    val backgroundColor: String?,
    val borderStyle: String?
) {
    companion object {
        fun from(popup: PopupEntity): PopupPreviewResponse {
            return PopupPreviewResponse(
                id = popup.id,
                title = popup.title,
                content = popup.content,
                width = popup.width,
                height = popup.height,
                imageUrl = popup.image?.let { "/files/${it.storedFileName}" }, // filePath 또는 storedFileName 사용
                backgroundColor = popup.backgroundColor,
                borderStyle = popup.borderStyle
            )
        }
    }
}