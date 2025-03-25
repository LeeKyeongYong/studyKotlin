package com.krstudy.kapi.domain.payments.dto

data class CashReceiptRequestDto(
    val amount: Int,
    val orderId: String,
    val orderName: String,
    val customerIdentityNumber: String,
    val type: String, // "소득공제" 또는 "지출증빙"
    val taxFreeAmount: Int? = null
)