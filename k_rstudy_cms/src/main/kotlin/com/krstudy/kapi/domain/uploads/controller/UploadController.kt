package com.krstudy.kapi.domain.uploads.controller


import com.krstudy.kapi.domain.uploads.service.FileServiceImpl
import com.krstudy.kapi.global.lgexecution.LogExecutionTime
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.ui.Model
import java.security.Principal

@Controller
@RequestMapping("/upload")
class UploadController {
    @GetMapping
    fun showUploadForm(): String {
        return "domain/upload/upload"
    }
}