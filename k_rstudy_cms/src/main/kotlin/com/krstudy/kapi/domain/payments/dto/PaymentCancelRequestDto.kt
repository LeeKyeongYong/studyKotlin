package com.krstudy.kapi.domain.payments.dto

data class PaymentCancelRequestDto(
    val paymentKey: String,
    val cancelReason: String,
    val cancelAmount: Int? = null,
    val refundReceiveAccount: RefundReceiveAccount? = null,
    val taxFreeAmount: Int? = null
)