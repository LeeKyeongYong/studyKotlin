package com.krstudy.kapi.domain.trade.dto

import java.math.BigDecimal
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class OrderForm(
    @field:NotNull
    var coinName: String? = null,

    @field:NotNull
    @field:Positive
    var price: BigDecimal? = null,

    @field:NotNull
    @field:Positive
    var quantity: BigDecimal? = null,

    var total: BigDecimal? = null
)