package com.krstudy.kapi.domain.member.controller

import com.krstudy.kapi.domain.emails.dto.EmailDto
import com.krstudy.kapi.domain.emails.service.EmailService
import com.krstudy.kapi.domain.member.datas.JoinForm
import com.krstudy.kapi.domain.member.datas.RegistrationQueue
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.domain.passwd.dto.PasswordChangeDto
import com.krstudy.kapi.domain.passwd.service.PasswordChangeHistoryService
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.global.lgexecution.LogExecutionTime
import com.krstudy.kapi.standard.base.MemberPdfView
import jakarta.mail.MessagingException
import jakarta.validation.Valid
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*
import javax.imageio.ImageIO


@Slf4j
@Controller
@RequestMapping("/member") // 회원 관련 요청을 처리하는 컨트롤러
class MemberController(
    private val memberService: MemberService, // 회원 서비스에 대한 의존성 주입
    private val rq: ReqData, // 요청 데이터에 대한 의존성 주입
    private val registrationQueue: RegistrationQueue, // 가입 요청 큐에 대한 의존성 주입
    private val passwordEncoder: PasswordEncoder, // 비밀번호 인코더 주입
    private val emailService: EmailService,
    private val passwordChangeHistoryService: PasswordChangeHistoryService
) {

    @Value("\${spring.mail.username}")
    lateinit var serviceEmail: String

    private val log: Logger = LoggerFactory.getLogger(MemberController::class.java) // Logger 인스턴스 생성

    /**
     * 회원 가입 페이지를 보여주는 메소드.
     * 인증되지 않은 사용자만 접근할 수 있습니다.
     * @return 가입 페이지의 뷰 이름
     */
    @PreAuthorize("isAnonymous() or hasRole('ADMIN')") // 인증되지 않은 사용자, 관리자가 접근 가능
    @GetMapping("/join") // GET 요청에 대해 /member/join 경로로 매핑
    @LogExecutionTime // 메소드 실행 시간 로그 기록
    fun showJoin(): String {
        log.info("showJoin() method called") // 메소드 호출 로그 기록
        return "domain/member/join" // 가입 페이지의 뷰 경로 반환
    }

    /**
     * 회원 가입 요청을 처리하는 메소드.
     * 폼 데이터를 검증하고, 큐에 가입 요청을 추가한 후 성공 응답을 생성합니다.
     * @param joinForm 가입 폼 데이터
     * @return 가입 성공 후 리디렉션할 경로
     */
    @PostMapping("/join") // POST 요청에 대해 /member/join 경로로 매핑
    @LogExecutionTime // 메소드 실행 시간 로그 기록
    suspend fun join(
        @Valid @ModelAttribute joinForm: JoinForm, // 가입 폼 데이터
        bindingResult: BindingResult, // 검증 결과
        redirectAttributes: RedirectAttributes // 리디렉션 시 사용할 속성
    ): String {
        // 검증 에러가 있는 경우 처리
        if (bindingResult.hasErrors()) {
            bindingResult.allErrors.forEach { error -> // 모든 에러에 대해
                val fieldName = (error as FieldError).field // 에러 필드명
                val errorMessage = error.defaultMessage // 에러 메시지
                redirectAttributes.addFlashAttribute("error_$fieldName", errorMessage) // 에러 메시지 플래시 속성 추가
            }
            return "redirect:/member/join" // 가입 페이지로 리디렉션
        }

        // 사용자 ID 중복 체크
        if (memberService.findByUserid(joinForm.userid) != null) { // ID가 이미 존재하는 경우
            redirectAttributes.addFlashAttribute("error_userid", "이미 등록된 사용자 ID입니다.") // 에러 메시지 추가
            return "redirect:/member/join" // 가입 페이지로 리디렉션
        }

        log.info("회원가입 시도: userid=${joinForm.userid}, username=${joinForm.username}, userEmail=${joinForm.userEmail}") // 가입 시도 로그 기록

        // 이미지 파일 처리
        val (imageBytes, imageType) = if (joinForm.image != null && !joinForm.image!!.isEmpty) { // 이미지 파일이 있는 경우
            val contentType = joinForm.image!!.contentType // 이미지의 콘텐츠 타입

            // 이미지 바이트로부터 실제 형식 검증
            val isImageValid = ImageIO.read(ByteArrayInputStream(joinForm.image!!.bytes)) != null // 이미지 검증

            if (contentType != null && isImageValid) { // 유효한 이미지인 경우
                getImageBytes(joinForm.image!!) to contentType // 이미지 파일의 바이트와 타입을 반환
            } else {
                getDefaultImageBytes() to null // 이미지가 아니면 기본 이미지를 사용하고 타입은 null로 처리
            }
        } else {
            getDefaultImageBytes() to null // 파일이 없으면 기본 이미지 사용, 타입은 null
        }

        // 회원 등록 로직에 imageType을 추가로 처리할 수 있습니다.
        registrationQueue.enqueue(
            joinForm.userid,
            joinForm.username,
            joinForm.password,
            joinForm.userEmail,
            imageType,
            imageBytes

        ) // 회원 가입 요청 큐에 추가

        redirectAttributes.addFlashAttribute("userid", joinForm.userid) // 가입 성공 후 사용자 ID 저장
        // 로그인한 사용자의 권한을 확인하여 리디렉션 경로 설정
        val isAdmin = rq.isAdmin()
        return if (isAdmin) "redirect:/adm" else "redirect:/member/login"
    }

    // 이미지 파일을 ByteArray로 변환
    private fun getImageBytes(image: MultipartFile): ByteArray {
        return image.bytes // 이미지 파일 바이트 반환
    }

    // 기본 이미지 바이트를 가져오는 함수
    private fun getDefaultImageBytes(): ByteArray {
        val inputStream = this::class.java.classLoader.getResourceAsStream("gen/images/notphoto.jpg") // 기본 이미지 파일 경로
            ?: throw FileNotFoundException("Default image not found in resources") // 이미지 파일이 없으면 예외 발생

        return inputStream.readBytes().also {
            inputStream.close() // InputStream 닫기
        }
    }

    /**
     * 로그인 페이지를 보여주는 메소드.
     * 인증되지 않은 사용자만 접근할 수 있습니다.
     * @return 로그인 페이지의 뷰 이름
     */
    @GetMapping("/login") // GET 요청에 대해 /member/login 경로로 매핑
    @LogExecutionTime // 메소드 실행 시간 로그 기록
    fun showLogin(): String {
        return "domain/member/login" // 로그인 페이지의 뷰 경로 반환
    }

    /**
     * 회원의 PDF 정보를 보여주는 메소드.
     * @param id 회원 ID
     * @return ModelAndView 객체
     */
    @GetMapping("/view_pdf") // 특정 회원의 PDF 정보를 조회하는 GET 요청
    fun viewPdf(@RequestParam(value = "id") id: Long): ModelAndView {
        val mav = ModelAndView()
        mav.view = MemberPdfView() // PDF 뷰 설정
        mav.addObject("member", memberService.getMemberByNo(id))
        return mav
    }

    /**
     * 특정 회원의 이미지를 가져오는 메소드.
     * @param id 회원 ID
     * @return 이미지 바이트 배열을 포함한 ResponseEntity
     */
    @GetMapping("/image/{id}") // 특정 회원의 이미지를 가져오는 GET 요청
    fun getImage(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val member: Member? = memberService.getMemberByNo(id)

        if (member == null || member.image == null) {
            return ResponseEntity.notFound().build()
        }

        val contentType = member.imageType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE

        return ResponseEntity.ok()
            .contentType(MediaType.valueOf(contentType))
            .body(member.image)
    }

    /**
     * 특정 회원 정보를 조회하는 메소드.
     * @param id 회원 ID
     * @param model 모델 객체
     * @return 상세 페이지의 뷰 이름
     */
    @GetMapping("/view") // 특정 회원 정보 조회를 위한 GET 요청
    fun viewMemberByNo(@RequestParam(value = "id") id: Long, model: Model): String {
        model.addAttribute("member", memberService.getMemberByNo(id)) // 회원 정보를 모델에 추가
        return "domain/member/view" // 상세 페이지의 뷰 경로 반환
    }

    /**
     * 특정 회원을 삭제하는 메소드.
     * @param id 회원 ID
     * @param attr 리디렉션 시 사용할 속성
     * @return 리디렉션할 경로
     */
    @GetMapping("/remove") // 특정 회원 삭제를 위한 GET 요청
    fun removeMemberByNo(@RequestParam(value = "id") id: Long, attr: RedirectAttributes): String {
        memberService.removeMember(id) // 회원 삭제
        attr.addFlashAttribute("msg", "회원이 삭제되었습니다.") // 삭제 메시지 추가
        return "redirect:/adm" // 회원 목록 페이지로 리디렉션
    }

    @GetMapping("/find")
    fun showFindAccountForm(): String {
        return "domain/member/find-account"
    }


    @PostMapping("/find")
    fun findMemberInfo(
        @RequestParam("username") username: String,
        @RequestParam("userEmail") userEmail: String,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val member: Optional<Member> = memberService.findMember(username, userEmail, "WEB")

        if (member.isPresent) {
            val foundMember = member.get()

            val encodedPath = encodeUrl("${foundMember.id}")
            val resetPasswordUrl = "http://localhost:8090/redirect?redirectCount=$encodedPath"
            println("이메일인증: $resetPasswordUrl")

            val emailDto = EmailDto(
                id = foundMember.id,
                serviceEmail = serviceEmail, // Use the injected serviceEmail as the serviceEmail parameter
                customEmail = serviceEmail, // Use the injected serviceEmail as the customEmail
                receiverEmail = foundMember.userEmail,
                title = "${foundMember.username}님 안녕하세요. 찾으시는....",
                content = "<html><body>${foundMember.username}님의 <br />" +
                        "<a href=\"$resetPasswordUrl\">나의 계정 찾기</a>에서 찾으실 수 있습니다.</body></html>"
            )

            emailService.sendSimpleVerificationMail(emailDto)

            redirectAttributes.addFlashAttribute("message", "메일이 발송되었습니다.")
        } else {
            redirectAttributes.addFlashAttribute("message", "해당 정보를 찾을 수 없습니다.")
        }

        return "redirect:/member/find"
    }

    fun encodeUrl(path: String): String {
        return Base64.getUrlEncoder().encodeToString(path.toByteArray(StandardCharsets.UTF_8))
    }

    @GetMapping("/myinfo")
    fun showMyInfo(model: Model): String {
        val member = rq.getMember() ?: throw IllegalStateException("로그인이 필요합니다")
        val passwordHistory = passwordChangeHistoryService.getPasswordChangeHistory(member.id)

        model.addAttribute("member", member)
        model.addAttribute("passwordHistory", passwordHistory)

        return "domain/member/myinfo"
    }

    @PostMapping("/password/change")
    fun changePassword(
        @Valid @ModelAttribute passwordChangeDto: PasswordChangeDto,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "입력값이 올바르지 않습니다")
            return "redirect:/member/myinfo"
        }

        try {
            val member = rq.getMember() ?: throw IllegalStateException("로그인이 필요합니다")
            memberService.changePassword(
                memberId = member.id,
                currentPassword = passwordChangeDto.currentPassword,
                newPassword = passwordChangeDto.newPassword,
                changeReason = passwordChangeDto.changeReason,
                signatureData = passwordChangeDto.signatureData // Canvas 데이터 전달
            )
            redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }

        return "redirect:/member/myinfo"
    }

}
