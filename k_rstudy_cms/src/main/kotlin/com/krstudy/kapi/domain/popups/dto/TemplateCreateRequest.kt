package com.krstudy.kapi.domain.popups.dto

data class TemplateCreateRequest(
    val name: String,
    val width: Int,
    val height: Int,
    val backgroundColor: String = "#ffffff",
    val borderStyle: String = "1px solid #000000",
    val content: String = "",
    val isDefault: Boolean = false
)