package com.krstudy.kapi.domain.emails.controller

import com.krstudy.kapi.domain.emails.dto.EmailDto
import com.krstudy.kapi.domain.emails.service.EmailService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/mymail")
@Controller
class EmailSendController(
    private val emailService: EmailService
) {
    @Value("\${spring.mail.username}")
    lateinit var serviceEmail: String

    @GetMapping("/sendForm")
    fun showEmailSendForm(model: Model): String {
        model.addAttribute("emailDto", EmailDto(serviceEmail = serviceEmail, receiverEmail = null))
        return "domain/email/email_send_form"
    }

    @PostMapping("/sendEmail")
    fun sendEmail(
        @RequestParam("customEmail") customEmail: String,
        @RequestParam("receiverEmail") receiverEmail: String,
        @RequestParam("title") title: String,
        @RequestParam("content") content: String,
        @RequestParam("attachment", required = false) attachment: MultipartFile?
    ): String {
        val emailDto = EmailDto(
            serviceEmail = customEmail,
            customEmail = customEmail,
            title = title,
            content = content,
            receiverEmail = receiverEmail,
            attachment = attachment
        )
       emailService.sendSimpleVerificationMail(emailDto)
        return "redirect:/messages" // Redirect to a success page
    }

    @GetMapping("/verifyCode")
    fun verifycodForm(): String {
        return "domain/email/verify-code"
    }

}