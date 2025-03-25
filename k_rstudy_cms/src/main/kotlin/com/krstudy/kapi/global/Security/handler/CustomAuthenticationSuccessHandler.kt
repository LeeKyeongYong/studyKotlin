package com.krstudy.kapi.global.Security.handler

import com.itextpdf.text.log.LoggerFactory
import com.krstudy.kapi.domain.member.service.AuthTokenService
import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.global.https.ReqData
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
@Transactional(readOnly = true)
class CustomAuthenticationSuccessHandler(
    private val memberService: MemberService,
    private val rq: ReqData,
    private val authTokenService: AuthTokenService
) : AuthenticationSuccessHandler {

    private val logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler::class.java)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val member = memberService.getMemberByAuthentication(authentication)
            member?.let {
                // JWT 토큰 생성 및 쿠키 설정
                val accessToken = authTokenService.genAccessToken(it)  // Member 객체 직접 전달
                rq.setCookie("accessToken", accessToken, 60 * 10) // 10분

                // 리프레시 토큰 설정
                val refreshToken = authTokenService.genRefreshToken(it.id.toString())  // ID를 문자열로 변환
                memberService.updateMemberJwtToken(it.id!!, refreshToken)
                rq.setCookie("refreshToken", refreshToken, 60 * 60 * 24 * 7) // 7일
            }

            // 메인 페이지로 리다이렉트
            response.sendRedirect("/")
        } catch (e: Exception) {
            logger.error("Authentication success handling failed", e)
            response.sendRedirect("/?error=" + URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8))
        }
    }
}