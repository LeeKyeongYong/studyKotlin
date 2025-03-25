package com.krstudy.kapi.global.Security.filter

import org.springframework.context.annotation.Lazy
import org.slf4j.LoggerFactory
import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.global.https.RespData
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
@Component
@RequiredArgsConstructor
class JwtAuthenticationFilter(
    private val rq: ReqData,
    @Lazy private val memberService: MemberService
) : OncePerRequestFilter() {

    @Throws(Exception::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            handleCookieBasedAuth(request)
        } catch (e: Exception) {
            logger.error("Authentication error: ${e.message}")
        }
        filterChain.doFilter(request, response)
    }

    private fun handleAuthentication(request: HttpServletRequest, response: HttpServletResponse) {
        val accessToken = rq.getCookieValue("accessToken", "")
        val refreshToken = rq.getCookieValue("refreshToken", "")

        when {
            !accessToken.isNullOrBlank() && memberService.validateToken(accessToken) -> {
                val securityUser = memberService.getUserFromAccessToken(accessToken)
                rq.setLogin(securityUser)
            }
            !refreshToken.isNullOrBlank() -> {
                try {
                    val rs = memberService.refreshAccessToken(refreshToken)
                    rs.data?.let { newAccessToken ->
                        rq.setCrossDomainCookie("accessToken", newAccessToken)
                        val securityUser = memberService.getUserFromAccessToken(newAccessToken)
                        rq.setLogin(securityUser)
                    }
                } catch (e: Exception) {
                    logger.error("Token refresh failed: ${e.message}")
                }
            }
        }
    }

    private fun handleTokenAuth(
        refreshToken: String,
        accessToken: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val validatedAccessToken = if (!memberService.validateToken(accessToken)) {
                // 액세스 토큰이 유효하지 않은 경우
                val rs: RespData<String> = memberService.refreshAccessToken(refreshToken)
                rs.data?.also { newAccessToken ->
                    rq.setHeader("Authorization", "Bearer $refreshToken $newAccessToken")
                } ?: run {
                    filterChain.doFilter(request, response)
                    return
                }
            } else {
                accessToken
            }

            val securityUser = memberService.getUserFromAccessToken(validatedAccessToken)
            rq.setLogin(securityUser)
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            logger.error("Token authentication error", e)
            filterChain.doFilter(request, response)
        }
    }

    private fun handleCookieBasedAuth(request: HttpServletRequest) {
        val accessToken = rq.getCookieValue("accessToken", "")
        val refreshToken = rq.getCookieValue("refreshToken", "")

        if (!accessToken.isNullOrBlank() && memberService.validateToken(accessToken)) {
            val securityUser = memberService.getUserFromAccessToken(accessToken)
            rq.setLogin(securityUser)
            return
        }

        if (!refreshToken.isNullOrBlank()) {
            try {
                val result = memberService.refreshAccessToken(refreshToken)
                if (result.isSuccess() && result.data != null) {
                    rq.setCrossDomainCookie("accessToken", result.data)
                    val securityUser = memberService.getUserFromAccessToken(result.data)
                    rq.setLogin(securityUser)
                }
            } catch (e: Exception) {
                logger.error("Token refresh failed: ${e.message}")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
}