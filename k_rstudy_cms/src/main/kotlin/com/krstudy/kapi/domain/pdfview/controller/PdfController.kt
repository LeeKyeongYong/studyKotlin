package com.krstudy.kapi.domain.pdfview.controller

import com.krstudy.kapi.domain.uploads.service.FileServiceImpl
import org.apache.pdfbox.pdmodel.PDDocument
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.File

@Controller
@RequestMapping("/uploadPdf")
class PdfController(
    private val fileService: FileServiceImpl
) {

    companion object {
        private val logger = LoggerFactory.getLogger(PdfController::class.java)
    }

    @GetMapping
    fun listDocuments(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        model: Model
    ): String {
        val documents = fileService.getAllActivePdfFiles(page - 1, size)
        model.addAttribute("documents", documents.content)
        model.addAttribute("page", page)
        model.addAttribute("totalPages", documents.totalPages)
        return "domain/upload/pdfMain"
    }

    @GetMapping("/view/{fileId}")
    fun viewPdf(
        @PathVariable fileId: Long,
        @RequestParam(defaultValue = "1") page: Int,
        model: Model
    ): String {
        try {
            val file = fileService.getFileById(fileId)
            val pdfFile = File(file.filePath)

            PDDocument.load(pdfFile).use { document ->
                val pageCount = document.numberOfPages

                model.addAttribute("file", file)
                model.addAttribute("currentPage", page)
                model.addAttribute("totalPages", pageCount)
                model.addAttribute("page", page)

                return "domain/upload/pdfView"
            }
        } catch (e: Exception) {
            logger.error("PDF 뷰어 로드 실패", e)
            model.addAttribute("error", "PDF 파일을 불러오는 중 오류가 발생했습니다.")
            return "redirect:/uploadPdf"
        }
    }

}