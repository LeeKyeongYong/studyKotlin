package com.krstudy.kapi.global.https

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.Security.SecurityUser
import com.krstudy.kapi.global.app.AppConfig
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseCookie
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Optional

@Component
@RequestScope
class ReqData(
    private val resp: HttpServletResponse
) {
    private val req: HttpServletRequest
        get() = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private var user: SecurityUser? = null
    private var member: Member? = null
    private var isLogin: Boolean? = null
    private var isAdmin: Boolean? = null

    fun setHeader(name: String, value: String) {
        resp.setHeader(name, value)
    }

    fun getHeader(name: String, defaultValue: String): String {
        return req.getHeader(name) ?: defaultValue
    }

    fun setStatusCode(statusCode: Int) {
        resp.status = statusCode
    }

    fun isApi(): Boolean {
        val xRequestedWith = req.getHeader("X-Requested-With")
        return "XMLHttpRequest" == xRequestedWith
    }

    fun setCookie(name: String, value: String, maxAgeInSeconds: Int = -1) {
        val cookieDomain = AppConfig.getSiteCookieDomain()
        val cookie = Cookie(name, value).apply {
            path = "/"
            domain = cookieDomain
            maxAge = maxAgeInSeconds
        }
        resp.addCookie(cookie)
    }

    private fun getSiteCookieDomain(): String? {
        val cookieDomain = AppConfig.getSiteCookieDomain()
        return if (cookieDomain != "localhost") ".$cookieDomain" else null
    }

    fun setCrossDomainCookie(name: String, value: String) {
        val cookie = ResponseCookie.from(name, value)
            .path("/")
            .domain(getSiteCookieDomain())
            .sameSite("Strict")
            .secure(true)
            .httpOnly(true)
            .build()

        resp.addHeader("Set-Cookie", cookie.toString())
    }

    fun removeCrossDomainCookie(name: String) {
        removeCookie(name)

        val cookie = ResponseCookie.from(name, "")
            .path("/")
            .maxAge(0)
            .domain(getSiteCookieDomain())
            .secure(true)
            .httpOnly(true)
            .build()

        resp.addHeader("Set-Cookie", cookie.toString())
    }

    fun getCookie(name: String): Cookie? {
        val cookies = req.cookies ?: return null
        return cookies.firstOrNull { it.name == name }
    }

    fun getCookieValue(name: String, defaultValue: String): String {
        return getCookie(name)?.value ?: defaultValue
    }

    private fun getCookieAsLong(name: String, defaultValue: Int): Long {
        val value = getCookieValue(name, "")
        return value.toLongOrNull() ?: defaultValue.toLong()
    }

    fun removeCookie(name: String) {
        val cookie = getCookie(name) ?: return
        cookie.path = "/"
        cookie.maxAge = 0
        resp.addCookie(cookie)
    }

    fun getMember(): Member? {
        if (isLogout()) return null

        if (member == null) {
            member = entityManager.getReference(Member::class.java, getUser()?.id)
        }

        return member
    }

    fun isAdmin(): Boolean {
        if (isLogout()) return false

        if (isAdmin == null) {
            isAdmin = getUser()?.authorities?.any { it.authority == "ROLE_ADMIN" } ?: false
        }

        return member?.isAdmin ?: false
    }

    fun isLogout(): Boolean {
        return !isLogin()
    }

    fun isLogin(): Boolean {
        if (isLogin == null) getUser()
        return isLogin == true
    }

    fun setLogin(securityUser: SecurityUser?) {
        if (securityUser == null) {
            throw IllegalArgumentException("SecurityUser cannot be null")
        }
        SecurityContextHolder.getContext().authentication = securityUser.genAuthentication()
    }

    fun getUser(): SecurityUser? {
        if (isLogin == null) {
            user = Optional.ofNullable(SecurityContextHolder.getContext())
                .map { it.authentication }
                .filter { it.principal is SecurityUser }
                .map { it.principal as SecurityUser }
                .orElse(null)

            isLogin = user != null
        }

        return user
    }

    fun setLogout() {
        removeCrossDomainCookie("accessToken")
        removeCrossDomainCookie("refreshToken")
        SecurityContextHolder.getContext().authentication = null
    }

    fun destroySession() {
        req.session.invalidate()
    }

    fun getCurrentUrlPath(): String {
        return req.requestURI // 요청 URI를 반환
    }

    fun setAttribute(key: String, value: Any) {
        req.setAttribute(key, value)
    }

    fun redirect(url: String, msg: String): String {
        val urlBits = url.split("#", limit = 2)
        val newUrl = urlBits[0]
        val encodedMsg = URLEncoder.encode(msg, StandardCharsets.UTF_8)

        val sb = StringBuilder()
        sb.append("redirect:")
        sb.append(newUrl)

        if (encodedMsg.isNotEmpty()) {
            sb.append("?msg=")
            sb.append(encodedMsg)
        }

        if (urlBits.size == 2) {
            sb.append("#")
            sb.append(urlBits[1])
        }

        return sb.toString()
    }

    fun getCurrentQueryStringWithoutParam(paramToExclude: String): String {
        val queryParams = req.queryString?.split("&")?.filter { !it.startsWith("$paramToExclude=") } ?: listOf()
        return if (queryParams.isNotEmpty()) "?" + queryParams.joinToString("&") else ""
    }

    fun isFrontUrl(url: String): Boolean {
        return url.startsWith(AppConfig.getSiteFrontUrl())
    }

}