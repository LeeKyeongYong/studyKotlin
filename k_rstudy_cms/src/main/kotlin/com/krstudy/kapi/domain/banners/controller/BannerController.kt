package com.krstudy.kapi.domain.banners.controller

import com.krstudy.kapi.domain.banners.service.BannerService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class BannerController(
    private val bannerService: BannerService
) {
    @GetMapping("/adm/banners")
    fun bannerManagement(model: Model): String {
        val banners = bannerService.getActiveBanners()
        model.addAttribute("banners", banners)
        return "domain/home/adm/managementBanner"
    }

    @GetMapping("/adm/banners/create")
    fun createBannerForm(model: Model): String {
        return "domain/home/adm/createBanner"
    }
}