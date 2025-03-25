package com.krstudy.kapi.domain.member.entity

import org.springframework.security.core.GrantedAuthority

interface RoleStrategy {
    fun getAuthorities(roleType: String?, userid: String): Collection<GrantedAuthority>
}