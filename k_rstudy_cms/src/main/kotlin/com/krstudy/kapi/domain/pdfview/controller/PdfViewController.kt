package com.krstudy.kapi.domain.uploads.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.data.domain.Page
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import com.krstudy.kapi.domain.uploads.service.FileServiceImpl
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import org.springframework.core.io.Resource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import java.nio.file.Path
import java.nio.file.Files
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/pdf")
class PdfRestController(
    private val fileService: FileServiceImpl
) {
    private val logger = LoggerFactory.getLogger(PdfRestController::class.java)

    data class PdfResponse(
        val id: Long,
        val originalFileName: String,
        val fileSize: Long,
        val contentType: String,
        val pageCount: Int? = null
    )

    @PostMapping("/upload")
    fun uploadPdf(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("title") title: String,  // title 파라미터 추가
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, Any>> {
        return try {
            if (file.contentType != "application/pdf") {
                return ResponseEntity.badRequest()
                    .body(mapOf("resultCode" to "400", "message" to "PDF 파일만 업로드 가능합니다."))
            }

            val userId = userDetails.username
            val savedFile = fileService.uploadFiles(arrayOf(file), userId).firstOrNull()
                ?: throw RuntimeException("파일 업로드 실패")

            ResponseEntity.ok(mapOf(
                "resultCode" to "200",
                "message" to "파일이 성공적으로 업로드되었습니다.",
                "fileId" to savedFile.id
            ))
        } catch (e: Exception) {
            logger.error("PDF 업로드 실패", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("resultCode" to "500", "message" to "파일 업로드 중 오류가 발생했습니다."))
        }
    }

    @GetMapping("/{fileId}")
    fun getPdfInfo(@PathVariable fileId: Long): ResponseEntity<PdfResponse> {
        return try {
            val file = fileService.getFileById(fileId)
            val pdfFile = File(file.filePath)

            val pageCount = PDDocument.load(pdfFile).use { document ->
                document.numberOfPages
            }

            val response = PdfResponse(
                id = file.id,
                originalFileName = file.originalFileName,
                fileSize = file.fileSize,
                contentType = file.contentType,
                pageCount = pageCount
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("PDF 정보 조회 실패", e)
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{fileId}/pages/{page}")
    fun getPdfPage(
        @PathVariable fileId: Long,
        @PathVariable page: Int,
        @RequestParam(defaultValue = "150") dpi: Float
    ): ResponseEntity<ByteArray> {
        return try {
            val file = fileService.getFileById(fileId)
            val pdfFile = File(file.filePath)

            PDDocument.load(pdfFile).use { document ->
                if (page < 1 || page > document.numberOfPages) {
                    return ResponseEntity.badRequest()
                        .body("유효하지 않은 페이지 번호입니다.".toByteArray())
                }

                val renderer = PDFRenderer(document)
                val image = renderer.renderImageWithDPI(page - 1, dpi)

                val baos = ByteArrayOutputStream()
                ImageIO.write(image, "png", baos)

                ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray())
            }
        } catch (e: Exception) {
            logger.error("PDF 페이지 렌더링 실패", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/list")
    fun listPdfFiles(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<PdfResponse>> {
        return try {
            val pdfFiles = fileService.getAllActivePdfFiles(page, size)
            val response = pdfFiles.map { file ->
                PdfResponse(
                    id = file.id,
                    originalFileName = file.originalFileName,
                    fileSize = file.fileSize,
                    contentType = file.contentType
                )
            }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("PDF 목록 조회 실패", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    @GetMapping("/{fileId}/download")
    fun downloadPdf(@PathVariable fileId: Long): ResponseEntity<Resource> {
        return try {
            val file = fileService.getFileById(fileId)
            val path = Path.of(file.filePath)
            val resource = InputStreamResource(Files.newInputStream(path))

            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"${URLEncoder.encode(file.originalFileName, StandardCharsets.UTF_8.name())}\"")
                .body(resource)
        } catch (e: Exception) {
            logger.error("PDF 다운로드 실패", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }



}