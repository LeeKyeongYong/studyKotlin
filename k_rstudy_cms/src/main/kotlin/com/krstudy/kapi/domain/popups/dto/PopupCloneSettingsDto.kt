package com.krstudy.kapi.domain.popups.dto

data class PopupCloneSettingsDto(
    val inheritTargetRoles: Boolean = true,
    val inheritDisplayPages: Boolean = true,
    val inheritSchedule: Boolean = false,
    val newTitle: String? = null
)