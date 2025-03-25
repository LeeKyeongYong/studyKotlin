package com.krstudy.kapi.domain.home.Controller

import com.krstudy.kapi.domain.member.service.MemberService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView
import java.nio.charset.StandardCharsets
import java.util.Base64


@Controller
class RedirectController(
    private val memberService: MemberService // 회원 서비스에 대한 의존성 주입
) {
    @GetMapping("/redirect")
    fun handleRedirect(redirectCount: String,
                       model: Model): String {
        val decodedUrl = String(Base64.getUrlDecoder().decode(redirectCount), StandardCharsets.UTF_8)
        println("redirect 이메일인증: $decodedUrl")
        model.addAttribute("member", memberService.getMemberByNo(decodedUrl.toLong())) // 회원 정보를 모델에 추가
        return "domain/member/reset-password";
    }
}