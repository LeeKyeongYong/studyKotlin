package com.krstudy.kapi.domain.item.controller

import com.krstudy.kapi.domain.item.service.ItemService
import com.krstudy.kapi.domain.item.ano.ItemTypeValid
import com.krstudy.kapi.domain.item.entity.ItemDTO
import com.krstudy.kapi.domain.item.entity.ResponseDTO
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.slf4j.LoggerFactory

@OpenAPIDefinition(info = Info(title = "물품 처리요청 API", description = "물품 처리요청 API", version = "v1"))
@RestController
@RequestMapping("v1/item")
@Validated
@RequiredArgsConstructor
class ItemController(private val itemService: ItemService) {

    private val log = LoggerFactory.getLogger(ItemController::class.java)

    @Operation(summary = "물품등록 요청", description = "물품 등록을 진행할 수 있다.", tags = ["addItem"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "SUCCESS"),
        ApiResponse(responseCode = "501", description = "API EXCEPTION")
    )
    @PostMapping("/add/{itemType}")
    fun add(
        request: HttpServletRequest,
        @Valid @RequestBody itemDTO: ItemDTO,
        @ItemTypeValid @PathVariable itemType: String
    ): ResponseEntity<ResponseDTO> {
        val responseBuilder = ResponseDTO()

        var accountId = request.getHeader("accountId")?.replace("[", "")?.replace("]", "") ?: ""
        log.info("accountId = {}", accountId)

        itemDTO.itemType = itemType // Kotlin에서는 var 또는 val 속성을 사용하여 직접 접근
        itemService.insertItem(itemDTO, accountId)
        log.debug("request add item id = {}", itemDTO.id)

        responseBuilder.code = "200"
        responseBuilder.message = "success"
        return ResponseEntity.ok(responseBuilder)
    }
}
