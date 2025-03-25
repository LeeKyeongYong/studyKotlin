package com.krstudy.kapi.domain.member.controller

import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.domain.passwd.dto.MemberSearchDto
import com.krstudy.kapi.domain.passwd.service.PasswordChangeHistoryService
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.member.datas.MemberUpdateData
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/adm/member")
@PreAuthorize("hasRole('ROLE_ADMIN')")
class AdminMemberController(
    private val memberService: MemberService,
    private val passwordChangeHistoryService: PasswordChangeHistoryService,
    private val rq: ReqData
) {
    @GetMapping("/list")
    fun listMembers(
        @ModelAttribute searchDto: MemberSearchDto
    ): String {
        val members = memberService.searchMembers(searchDto)
        rq.setAttribute("members", members)
        rq.setAttribute("searchDto", searchDto)
        return "domain/home/adm/memList"
    }

    @GetMapping("/history/{memberId}")
    fun memberHistory(@PathVariable memberId: Long): String {
        val member = memberService.getMemberByNo(memberId)
            ?: throw IllegalArgumentException("회원을 찾을 수 없습니다")
        val history = passwordChangeHistoryService.getPasswordChangeHistory(memberId)
        rq.setAttribute("member", member)
        rq.setAttribute("history", history)
        return "domain/home/adm/userHistory"
    }

    @PostMapping("/update/{id}")
    fun updateMember(
        @PathVariable id: Long,
        @Valid @ModelAttribute updateData: MemberUpdateData,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "입력값이 올바르지 않습니다")
            return "redirect:/adm/member/list"
        }

        try {
            memberService.update(id, updateData, null, null)
            redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }
        return "redirect:/adm/member/list"
    }
}