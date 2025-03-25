package com.krstudy.kapi.standard.base

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class ImageValidator : ConstraintValidator<ValidImage, MultipartFile?> {
    private var maxSize: Long = 0
    private lateinit var allowedTypes: Array<String>

    override fun initialize(constraintAnnotation: ValidImage) {
        maxSize = constraintAnnotation.maxSize
        allowedTypes = constraintAnnotation.types
    }

    override fun isValid(file: MultipartFile?, context: ConstraintValidatorContext): Boolean {
        file ?: return true  // null은 @NotNull로 처리하도록 함

        try {
            // 1. 파일 크기 검증
            if (file.size > maxSize) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(
                    "파일 크기는 ${maxSize / 1024 / 1024}MB를 초과할 수 없습니다"
                ).addConstraintViolation()
                return false
            }

            // 2. 파일 타입 검증
            val contentType = file.contentType
            if (contentType == null || !allowedTypes.contains(contentType.lowercase())) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(
                    "허용되지 않는 파일 형식입니다. 허용된 형식: ${allowedTypes.joinToString(", ")}"
                ).addConstraintViolation()
                return false
            }

            // 3. 파일 시그니처 검증
            val bytes = file.bytes
            if (!isValidImageSignature(bytes)) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(
                    "올바르지 않은 이미지 파일입니다"
                ).addConstraintViolation()
                return false
            }

            // 4. 이미지 메타데이터 검증
            val inputStream = ByteArrayInputStream(bytes)
            val bufferedImage: BufferedImage? = ImageIO.read(inputStream)

            if (bufferedImage == null) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(
                    "이미지를 읽을 수 없습니다"
                ).addConstraintViolation()
                return false
            }

            // 이미지 크기 제한 (예: 최대 4000x4000)
            if (bufferedImage.width > 4000 || bufferedImage.height > 4000) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(
                    "이미지 크기가 너무 큽니다. 최대 4000x4000 픽셀까지 허용됩니다"
                ).addConstraintViolation()
                return false
            }

            return true

        } catch (e: Exception) {
            logger.error("이미지 검증 중 오류 발생", e)
            return false
        }
    }

    private fun isValidImageSignature(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false

        return when {
            // JPEG: FF D8 FF
            bytes.startsWith(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) -> true

            // PNG: 89 50 4E 47
            bytes.startsWith(byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())) -> true

            // GIF: 47 49 46 38
            bytes.startsWith(byteArrayOf(0x47.toByte(), 0x49.toByte(), 0x46.toByte(), 0x38.toByte())) -> true

            else -> false
        }
    }

    private fun ByteArray.startsWith(signature: ByteArray): Boolean {
        if (this.size < signature.size) return false
        return signature.withIndex().all { (i, byte) -> this[i] == byte }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ImageValidator::class.java)
    }
}