package com.krstudy.kapi.domain.trade.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class OrderRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    val userId: String,

    @field:NotBlank(message = "코인 코드는 필수입니다")
    val coinCode: String,

    @field:NotNull(message = "주문 가격은 필수입니다")
    @field:DecimalMin(value = "0.0", message = "주문 가격은 0보다 커야 합니다")
    val price: BigDecimal,

    @field:NotNull(message = "주문 수량은 필수입니다")
    @field:DecimalMin(value = "0.0", message = "주문 수량은 0보다 커야 합니다")
    val quantity: BigDecimal
)