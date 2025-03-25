package com.krstudy.kapi.domain.home.Controller

import com.krstudy.kapi.domain.member.service.MemberService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class AdmHomeController(
    private val service: MemberService // 생성자 주입 사용
) {

    @GetMapping("/adm")
    fun showMain(model: Model): String {
        model.addAttribute("members", service.getAllMembers())
        return "domain/home/adm/main"
    }

    @GetMapping("/adm/home/about")
    fun showAbout(): String {
        return "domain/home/adm/about"
    }
}
