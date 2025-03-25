package com.krstudy.kapi.global.Security.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class CustomOAuth2FailureHandler : AuthenticationFailureHandler {
    private val log = LoggerFactory.getLogger(CustomOAuth2FailureHandler::class.java)

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        log.error("OAuth2 authentication failed", exception.message)
        log.error("Auth2 login failed: ", exception)
        val errorMessage = "로그인에 실패했습니다. 관리자에게 문의하세요."
        val encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)
        response.sendRedirect("/?msg=$encodedErrorMessage")  // 루트 페이지로 리다이렉트
    }
}