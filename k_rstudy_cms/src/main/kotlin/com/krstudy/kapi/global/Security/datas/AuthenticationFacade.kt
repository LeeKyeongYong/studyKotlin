package com.krstudy.kapi.global.Security.datas

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {
    fun getCurrentUser(): UserDetails? {
        val authentication = SecurityContextHolder.getContext().authentication
        return when {
            authentication?.principal is UserDetails -> authentication.principal as UserDetails
            else -> null
        }
    }
}