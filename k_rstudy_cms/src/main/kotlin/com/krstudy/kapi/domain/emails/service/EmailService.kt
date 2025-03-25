package com.krstudy.kapi.domain.emails.service

import com.krstudy.kapi.domain.emails.dto.EmailDto
import com.krstudy.kapi.domain.emails.entity.VerificationCode
import com.krstudy.kapi.emails.repository.VerificationCodeRepository
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import com.krstudy.kapi.domain.emails.entity.Email
import com.krstudy.kapi.domain.emails.repository.EmailRepository
import com.krstudy.kapi.domain.files.repository.FileRepository
import com.krstudy.kapi.domain.files.service.FileService
import com.krstudy.kapi.domain.member.service.MemberService
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.web.multipart.MultipartFile
import org.springframework.context.annotation.Lazy

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val emailRepository: EmailRepository,
    @Lazy private val memberService: MemberService,
    private val fileService: FileService,  // FileService를 주입받도록 변경
) {
    private val EXPIRATION_TIME_IN_MINUTES = 5

    // 메일 발송 메소드
    fun sendSimpleVerificationMail(mailDto: EmailDto) {
        val verificationCode = generateVerificationCode() // 자동으로 createAt 설정됨

        // MimeMessage 사용하여 파일 첨부
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8") // UTF-8 인코딩 설정

        // `mailDto.serviceEmail`과 `mailDto.receiverEmail`이 null일 경우 기본값을 설정합니다.
        val senderEmail = mailDto.serviceEmail ?: throw IllegalArgumentException("Sender email is missing")
        val receiverEmail = mailDto.receiverEmail ?: throw IllegalArgumentException("Receiver email is missing")

        helper.setFrom(senderEmail) // 보내는사람
        helper.setTo(receiverEmail) // 받는사람
        helper.setSubject(mailDto.title)
        helper.setText(mailDto.content.takeIf { it.isNotBlank() } ?: verificationCode.generateCodeMessage(), true)

        // EmailLog 저장 (여기서 이메일 로그를 먼저 저장)
        val emailLog = Email(
            serviceEmail = mailDto.customEmail ?: senderEmail,  // `customEmail`이 null일 경우 `senderEmail` 사용
            title = mailDto.title,
            content = mailDto.content.takeIf { it.isNotBlank() } ?: verificationCode.generateCodeMessage(),
            receiverEmail = receiverEmail
        )

        println("이메일 로그 저장 전: $emailLog")

        val savedEmailLog = emailRepository.save(emailLog) // 이메일 발송 내용 저장
        println("이메일 로그 저장 후: $savedEmailLog")

        // 파일이 존재하는 경우 첨부
        mailDto.attachment?.let {
            if (!it.isEmpty) {
                helper.addAttachment(it.originalFilename ?: "attachment", it)
                fileService.saveFile(it, savedEmailLog.id ?: throw Exception("Email ID is null"), "EMAIL")
            }
        }

        mailSender.send(mimeMessage)
        verificationCodeRepository.save(verificationCode) // DB에 저장
    }

    // 인증 코드 검증 메소드
    fun verifyCode(code: String, verifiedAt: LocalDateTime) {
        val verificationCode = verificationCodeRepository.findByCode(code)
            ?: throw GlobalException(MessageCode.NOT_FOUND_RESOURCE)

        if (verificationCode.isExpired(verifiedAt)) {
            throw GlobalException(MessageCode.EXPIRED_VERIFICATION_CODE)
        }

        verificationCodeRepository.delete(verificationCode) // DB에서 삭제
    }

    // 인증 코드 생성 메소드
    private fun generateVerificationCode(): VerificationCode {
        val code = UUID.randomUUID().toString()
        return VerificationCode(
            code = code,
            expirationTimeInMinutes = EXPIRATION_TIME_IN_MINUTES
        )
    }
}
