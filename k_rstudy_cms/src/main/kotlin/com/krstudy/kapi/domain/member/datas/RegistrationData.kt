package com.krstudy.kapi.domain.member.datas


data class RegistrationData(
    val userid: String,
    val username: String,
    val password: String,
    val userEmail: String,
    val imageType: String? = null,
    val imageBytes: ByteArray? = null,

    val additionalFields: Map<String, Any> = emptyMap() // 추가 필드를 위한 Map
)