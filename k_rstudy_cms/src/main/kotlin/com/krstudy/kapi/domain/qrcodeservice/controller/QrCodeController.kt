package com.krstudy.kapi.domain.qrcodeservice.controller

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.krstudy.kapi.com.krstudy.kapi.domain.qrcodeservice.datas.QRCodeValidationRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import java.io.ByteArrayOutputStream
import com.krstudy.kapi.domain.qrcodeservice.service.QRCodeService
import lombok.extern.slf4j.Slf4j

@RestController
@RequestMapping("/v1/qrcode")
@Slf4j
class QRCodeController(private val qrCodeService: QRCodeService) {

    private val log = LoggerFactory.getLogger(QRCodeController::class.java)

    @GetMapping("/qr", produces = [MediaType.IMAGE_PNG_VALUE])
    fun createQRCode(): ResponseEntity<ByteArray> {
        val url = "https://velog.io/@sleekydevzero86/posts"
        //val url = "010-1234-5678"
        val width = 200
        val height = 200

        val qrCodeImage = qrCodeService.createQRCode(url, width, height)

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(qrCodeImage)
    }

    @PostMapping("/extract")
    fun validateQRCodeInfo(@RequestBody request: QRCodeValidationRequest): ResponseEntity<Map<String, String>> {
        val info = qrCodeService.extractInfoFromQRCode(request.info.toByteArray())
        val validationResult = info?.let { qrCodeService.validatePhoneNumber(it) } ?: "정보 추출 실패"
        return ResponseEntity.ok(mapOf("validationResult" to validationResult))
    }

}