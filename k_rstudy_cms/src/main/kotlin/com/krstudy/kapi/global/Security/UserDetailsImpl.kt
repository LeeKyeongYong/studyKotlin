package com.krstudy.kapi.global.Security

import com.krstudy.kapi.domain.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(
    val member: Member
) : UserDetails {
    val userid: String = member.userid

    override fun getAuthorities(): Collection<GrantedAuthority> = member.authorities
    override fun getPassword(): String = member.password
    override fun getUsername(): String = member.userid
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = member.useYn == "Y"
}
