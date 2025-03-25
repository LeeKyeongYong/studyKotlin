package com.krstudy.kapi.domain.popups.controller

import com.krstudy.kapi.domain.popups.service.PopupService
import com.krstudy.kapi.global.https.ReqData
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller

class PopController (
    private val rq: ReqData,
    private val popupService: PopupService
) {

    /**
     * 팝업 관리 목록 페이지
     */
    @GetMapping("/adm/popups")
    fun popupManagement(model: Model): String {
        val popups = popupService.getAllPopups()
        rq.setAttribute("popups", popups)
        return "domain/home/adm/managementPopup"
    }

    /**
     * 팝업 생성 폼 페이지
     */
    @GetMapping("/adm/popups/create")
    fun createPopupForm(
        @RequestParam templateId: Long?
    ): String {
        if (templateId != null) {
            val template = popupService.getTemplate(templateId)
            rq.setAttribute("template", template)
        }
        return "domain/home/adm/createPopup"
    }
    /**
     * 템플릿관리 페이지
     */
    @GetMapping("/adm/popups/templates")
    fun popupTemplates(): String {
        val defaultTemplates = popupService.getDefaultTemplates()
        val customTemplates = popupService.getCustomTemplates()

        rq.setAttribute("defaultTemplates", defaultTemplates)
        rq.setAttribute("customTemplates", customTemplates)

        return "domain/home/adm/popupTemplates"
    }

    @GetMapping("/adm/popups/template-preview/{id}")
    fun previewTemplate(@PathVariable id: Long): String {
        val template = popupService.getTemplate(id)
        rq.setAttribute("template", template)
        return "domain/home/adm/template-preview" // HTML 템플릿 경로 지정
    }

    /**
     * 팝업 수정 폼 페이지
     */
    @GetMapping("/adm/popups/edit/{id}")
    fun editPopupForm(@PathVariable id: Long): String {
        val popup = popupService.getPopup(id)
        rq.setAttribute("popup", popup)
        return "domain/home/adm/editPopup"  // 수정 폼 템플릿 경로
    }


}