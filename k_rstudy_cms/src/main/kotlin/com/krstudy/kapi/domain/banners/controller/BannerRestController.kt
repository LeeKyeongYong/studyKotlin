package com.krstudy.kapi.domain.banners.controller

import com.krstudy.kapi.domain.banners.dto.BannerCreateRequest
import com.krstudy.kapi.domain.banners.dto.BannerResponse
import com.krstudy.kapi.domain.banners.service.BannerService
import com.krstudy.kapi.global.exception.BannerCreationException
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/banners")
class BannerRestController(
    private val bannerService: BannerService
) {
    @PostMapping
    fun createBanner(
        @RequestPart("banner") request: BannerCreateRequest,
        @RequestPart("image") image: MultipartFile,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<Any> {
        try {
            println("=== Banner Creation Request ===")
            println("Request Data: $request")
            println("Image Name: ${image.originalFilename}")
            println("Image Size: ${image.size}")
            println("Content Type: ${image.contentType}")
            println("User: ${userDetails?.username}")

            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("error" to "Unauthorized"))
            }

            val banner = bannerService.createBanner(request, image, userDetails.username)
            return ResponseEntity.ok(banner)
        } catch (e: IllegalArgumentException) {
            println("Validation error: ${e.message}")
            return ResponseEntity.badRequest()
                .body(mapOf("error" to (e.message ?: "Invalid request")))
        } catch (e: EntityNotFoundException) {
            println("Not found error: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (e.message ?: "Resource not found")))
        } catch (e: BannerCreationException) {
            println("Banner creation error: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to (e.message ?: "Failed to create banner")))
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "An unexpected error occurred"))
        }
    }

    @GetMapping
    fun getActiveBanners(): ResponseEntity<List<BannerResponse>> {
        return ResponseEntity.ok(bannerService.getActiveBanners())
    }
}