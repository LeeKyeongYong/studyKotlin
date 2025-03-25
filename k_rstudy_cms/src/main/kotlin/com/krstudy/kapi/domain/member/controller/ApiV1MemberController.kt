package com.krstudy.kapi.domain.member.controller

import com.krstudy.kapi.domain.member.dto.MemberInfo
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.domain.passwd.service.PasswordChangeAlertService
import com.krstudy.kapi.global.exception.MessageCode
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.global.https.RespData
import com.krstudy.kapi.member.datas.MemberUpdateData
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import javax.imageio.ImageIO

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ApiV1MemberController(
    private val memberService: MemberService,
    @Autowired private val rq: ReqData,
    private val passwordChangeAlertService: PasswordChangeAlertService
) {

    private val logger = LoggerFactory.getLogger(ApiV1MemberController::class.java)

    @GetMapping("/socialLogin/{providerTypeCode}")
    fun socialLogin(@RequestParam(required = false) redirectUrl: String?, @PathVariable providerTypeCode: String): String {
        redirectUrl?.takeIf { rq.isFrontUrl(it) }?.let {
            rq.setCookie("redirectUrlAfterSocialLogin", it, 60 * 10)
        }
        return "redirect:/oauth2/authorization/$providerTypeCode"
    }

    @GetMapping("/current")
    fun getCurrentUser(): ResponseEntity<MemberInfo> {
        val currentUser = rq.getMember()
        return if (currentUser != null) {
            ResponseEntity.ok(MemberInfo(currentUser.id, currentUser.username))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
    
    //개인정보수정 추가
    @PutMapping("/{id}")
    fun updateMember(
        @PathVariable id: Long,
        @ModelAttribute @Valid updateData: MemberUpdateData,
        bindingResult: BindingResult
    ): ResponseEntity<RespData<Member?>> {

        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
            return ResponseEntity.badRequest()
                .body(RespData.of(MessageCode.BAD_REQUEST.code, "Validation failed", null))
        }

        if (updateData.nickname.isNullOrBlank() || updateData.userEmail.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(RespData.fromErrorCode(MessageCode.INVALID_INPUT))
        }

        if (!isValidEmail(updateData.userEmail!!)) {
            return ResponseEntity.badRequest().body(RespData.fromErrorCode(MessageCode.INVALID_EMAIL))
        }

        if (!updateData.password.isNullOrBlank() && updateData.password != updateData.passwordConfirm) {
            return ResponseEntity.badRequest().body(RespData.fromErrorCode(MessageCode.PASSWORD_MISMATCH))
        }

        // 이미지 처리
        val (imageBytes, imageType) = if (updateData.image != null && !updateData.image!!.isEmpty) {
            val isImageValid = ImageIO.read(ByteArrayInputStream(updateData.image!!.bytes)) != null
            if (isImageValid) {
                getImageBytes(updateData.image!!) to updateData.image!!.contentType
            } else {
                getDefaultImageBytes() to null
            }
        } else {
            getDefaultImageBytes() to null
        }

        // 멤버 업데이트 후 결과 반환
        // 기존 코드
        val result = memberService.update(id, updateData, imageBytes, imageType)
        return ResponseEntity.ok(RespData.of(MessageCode.SUCCESS.code, "회원 정보가 성공적으로 수정되었습니다.", result))

    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        return email.matches(emailRegex)
    }

    // 기본 이미지 바이트를 가져오는 함수
    private fun getDefaultImageBytes(): ByteArray {
        val inputStream = this::class.java.classLoader.getResourceAsStream("gen/images/notphoto.jpg") // 기본 이미지 파일 경로
            ?: throw FileNotFoundException("Default image not found in resources") // 이미지 파일이 없으면 예외 발생

        return inputStream.readBytes().also {
            inputStream.close() // InputStream 닫기
        }
    }

    // 이미지 파일을 ByteArray로 변환
    private fun getImageBytes(image: MultipartFile): ByteArray {
        return image.bytes // 이미지 파일 바이트 반환
    }

    @PostMapping("/password/auto-change")
    fun autoChangePassword(): ResponseEntity<RespData<Map<String, String>>> {
        val member = rq.getMember() ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(RespData.fromErrorCode(MessageCode.UNAUTHORIZED))

        val success = passwordChangeAlertService.autoChangePassword(member.id)

        return if (success) {
            ResponseEntity.ok(RespData.of(
                MessageCode.SUCCESS.code,
                "비밀번호가 성공적으로 변경되었습니다.",
                mapOf("message" to "비밀번호가 변경되었습니다.")
            ))
        } else {
            ResponseEntity.badRequest()
                .body(RespData.fromErrorCode(MessageCode.PASSWORD_MISMATCH))
        }
    }

}