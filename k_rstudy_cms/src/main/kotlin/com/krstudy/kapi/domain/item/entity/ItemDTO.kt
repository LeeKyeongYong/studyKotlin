package com.krstudy.kapi.domain.item.entity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import lombok.Data

@Data
data class ItemDTO(
    @Schema(description = "물품ID", example = "TESTID")
    @field:NotBlank(message = "ID는 필수 입력 값입니다.")
    @field:Size(max = 10, message = "ID는 크기 10이하까지 작성가능합니다.")
    val id: String,

    @Schema(description = "물품명", example = "과일")
    @field:Size(max = 20, message = "이름은 20자까지 작성가능합니다.")
    val name: String? = null,

    @Schema(description = "물품설명", example = "물품설명테스트")
    @field:Size(max = 200, message = "설명은 최대 200자까지 작성가능합니다.")
    val description: String? = null,

    @Schema(description = "물품유형", example = "F: 음식, C: 옷")
    var itemType: String? = null,  // var로 변경

    @Schema(description = "물품개수", example = "100")
    @field:Positive
    val count: Long,

    @Schema(hidden = true)
    val regDts: String? = null,

    @Schema(hidden = true)
    val updDts: String? = null
)
