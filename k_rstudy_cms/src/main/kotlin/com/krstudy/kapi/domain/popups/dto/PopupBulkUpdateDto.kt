package com.krstudy.kapi.domain.popups.dto

import com.krstudy.kapi.domain.popups.enums.PopupStatus

data class PopupBulkUpdateDto(
    val id: Long,
    val status: PopupStatus? = null,
    val priority: Int? = null,
    val startDateTime: String? = null,
    val endDateTime: String? = null
)