package com.krstudy.kapi.com.krstudy.kapi.domain.emails.controller

import com.krstudy.kapi.domain.emails.dto.EmailDto
import com.krstudy.kapi.domain.emails.service.EmailService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/email")
class ApiV1VerificationController(
    private val emailService: EmailService
) {

    // 필요한 추가 정보 설정
    @Value("\${spring.mail.username}")
    lateinit var serviceEmail: String

    // 인증 메일 전송
    @PostMapping("/send-verification")
    fun sendVerificationEmail(@RequestBody request: Map<String, String>): ResponseEntity<String> {
        val receiverEmail = request["receiverEmail"] ?: throw IllegalArgumentException("receiverEmail is required")
        val title = "인증코드 안내메일입니다."

        // EmailDto 생성
        val emailDetails = EmailDto(
            receiverEmail = receiverEmail,//받는사람
            serviceEmail = serviceEmail,//보내는사람
            title = title //제목
        )

        emailService.sendSimpleVerificationMail(emailDetails)
        return ResponseEntity.ok("인증메일이 성공적으로 발송 되었습니다..")
    }

    // 인증 코드 검증
    @PostMapping("/verify-code")
    fun verifyCode(@RequestParam code: String): ResponseEntity<String> {
        val verifiedAt = LocalDateTime.now()
        emailService.verifyCode(code, verifiedAt)
        return ResponseEntity.ok("Verification code verified successfully.")
    }

}