package com.krstudy.kapi.domain.home.Controller

import com.krstudy.kapi.domain.banners.service.BannerService
import com.krstudy.kapi.domain.passwd.service.PasswordChangeAlertService
import com.krstudy.kapi.domain.popups.entity.DeviceType
import com.krstudy.kapi.domain.popups.service.PopupService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.domain.post.service.PostService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model

@Controller
class HomeController(
    private val rq: ReqData,
    private val postService: PostService,
    private val bannerService: BannerService,
    private val popupService: PopupService,
    private val passwordChangeAlertService: PasswordChangeAlertService
) {
    @GetMapping("/")
    fun showMain(request: HttpServletRequest): String {
        try {
            // 디바이스 타입 감지
            val deviceType = if (request.getHeader("User-Agent")?.contains("Mobile") == true) {
                DeviceType.MOBILE
            } else {
                DeviceType.PC
            }

            // 현재 페이지 경로
            val currentPage = request.requestURI

            // 활성화된 팝업 조회
            val popups = popupService.getActivePopups(deviceType, currentPage)
            rq.setAttribute("popups", popups)

            // 배너 조회
            val banners = bannerService.getActiveBanners()
            rq.setAttribute("banners", banners)

            // 최신 게시물 조회
            val posts = postService.findTop30ByIsPublishedOrderByIdDesc(true)
            rq.setAttribute("posts", posts)

            rq.getMember()?.let { member ->
                passwordChangeAlertService.checkPasswordChangeNeeded(member.id)?.let { alert ->
                    rq.setAttribute("passwordChangeAlert", alert)
                }
            }

            return "domain/home/main"
        } catch (e: Exception) {
            e.printStackTrace()
            rq.setAttribute("error", "페이지 로딩 중 오류가 발생했습니다.")
            return "error/500"
        }
    }
}