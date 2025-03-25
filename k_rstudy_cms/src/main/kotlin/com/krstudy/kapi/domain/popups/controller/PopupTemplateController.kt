package com.krstudy.kapi.domain.popups.controller

import com.krstudy.kapi.domain.popups.dto.TemplateCreateRequest
import com.krstudy.kapi.domain.popups.dto.TemplateResponse
import com.krstudy.kapi.domain.popups.service.PopupService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/popup-templates")
class PopupTemplateController(
    private val popupService: PopupService
) {
    @GetMapping
    fun getTemplates(): ResponseEntity<Map<String, List<TemplateResponse>>> {
        val defaultTemplates = popupService.getDefaultTemplates()
        val customTemplates = popupService.getCustomTemplates()

        return ResponseEntity.ok(mapOf(
            "defaultTemplates" to defaultTemplates,
            "customTemplates" to customTemplates
        ))
    }

    @GetMapping("/{id}")
    fun getTemplate(@PathVariable id: Long): ResponseEntity<TemplateResponse> {
        val template = popupService.getTemplate(id)
        return ResponseEntity.ok(template)
    }

    @PostMapping
    fun createTemplate(
        @RequestBody request: TemplateCreateRequest,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<TemplateResponse> {
        val template = popupService.saveTemplate(request, user.username)
        return ResponseEntity.ok(template)
    }

    @PutMapping("/{id}")
    fun updateTemplate(
        @PathVariable id: Long,
        @RequestBody request: TemplateCreateRequest,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<TemplateResponse> {
        val template = popupService.updateTemplate(id, request, user.username)
        return ResponseEntity.ok(template)
    }

    @DeleteMapping("/{id}")
    fun deleteTemplate(@PathVariable id: Long): ResponseEntity<Void> {
        popupService.deleteTemplate(id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}/preview")
    fun previewTemplate(@PathVariable id: Long): ResponseEntity<TemplateResponse> {
        val template = popupService.previewTemplate(id)
        return ResponseEntity.ok(template)
    }
}