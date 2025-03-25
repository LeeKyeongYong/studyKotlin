package com.krstudy.kapi.global.Security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
    val id: Long,
    private val _username: String,
    private val _password: String,
    private val authorities: Collection<GrantedAuthority>,
    private val enabled: Boolean = true,
    private val accountNonExpired: Boolean = true,
    private val credentialsNonExpired: Boolean = true,
    private val accountNonLocked: Boolean = true
) : UserDetails, OAuth2User {

    override fun getUsername(): String = _username
    override fun getPassword(): String = _password

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun isAccountNonExpired(): Boolean = accountNonExpired
    override fun isAccountNonLocked(): Boolean = accountNonLocked
    override fun isCredentialsNonExpired(): Boolean = credentialsNonExpired
    override fun isEnabled(): Boolean = enabled

    fun genAuthentication(): Authentication {
        return UsernamePasswordAuthenticationToken(
            this,
            this.password,
            this.authorities
        )
    }

    override fun getAttributes(): Map<String, Any> {
        return emptyMap() // 빈 맵 반환
    }

    override fun getName(): String = _username
}