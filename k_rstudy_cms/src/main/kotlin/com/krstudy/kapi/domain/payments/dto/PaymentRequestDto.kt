package com.krstudy.kapi.domain.payments.dto

data class PaymentRequestDto(
    val paymentKey: String,
    val orderId: String,
    val amount: Int
)