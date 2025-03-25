package com.krstudy.kapi.domain.member.entity


import com.krstudy.kapi.domain.member.datas.M_Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class DefaultRoleStrategy : RoleStrategy {
    override fun getAuthorities(roleType: String?, userid: String): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()
        val role: M_Role = M_Role.fromRoleType(roleType) // 역할 찾기

        authorities.add(SimpleGrantedAuthority(role.authority))

        // 시스템 관리자 또는 admin 권한 추가
        if (userid.equals("system", ignoreCase = true) || userid.equals("admin", ignoreCase = true)) {
            authorities.add(SimpleGrantedAuthority(M_Role.ADMIN.authority))
        }

        return authorities
    }
}