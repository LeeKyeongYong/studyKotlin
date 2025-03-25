package com.krstudy.kapi.global.exception

import org.springframework.security.core.AuthenticationException
class CustomException(
    val errorCode: MessageCode
) : AuthenticationException(errorCode.message)
