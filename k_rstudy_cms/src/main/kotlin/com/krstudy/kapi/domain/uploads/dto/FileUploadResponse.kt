package com.krstudy.kapi.domain.uploads.dto

data class FileUploadResponse(
    val message: String,
    val fileIds: List<String>? = null
)