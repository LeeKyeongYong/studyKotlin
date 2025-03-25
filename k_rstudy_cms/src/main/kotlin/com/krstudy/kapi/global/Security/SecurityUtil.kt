package com.krstudy.kapi.global.Security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityUtil {

    fun getCurrentUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        // Principal에서 사용자 ID 추출
        return when (val principal = authentication.principal) {
            is UserDetailsImpl -> principal.userid
            else -> null
        }
    }
}