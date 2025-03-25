package com.krstudy.kapi.global.Security.handler

import com.krstudy.kapi.global.exception.MessageCode
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class CustomAuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    private val log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler::class.java)

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        val errorMessage: String = when (exception) {
            is UsernameNotFoundException -> {
                log.info("exception.message: ${exception?.message}")

                if (exception.message == MessageCode.LOGIN_DISABLED_USER.message) {
                    "로그인할 수 없는 아이디입니다."
                } else {
                    "아이디 또는 비밀번호가 틀렸습니다."
                }
            }
            else -> "관리자에게 문의 하세요."
        }
        log.info("exception.message2: ${exception?.message}")
        response?.sendRedirect("/member/login?failMsg=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
    }
}
