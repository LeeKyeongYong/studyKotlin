package com.krstudy.kapi.domain.payments.dto

data class TossPaymentResponse(
    val mId: String? = null,
    val version: String? = null,
    val paymentKey: String? = null,
    val status: String? = null,
    val orderId: String? = null,
    val orderName: String? = null,
    val requestedAt: String? = null,
    val approvedAt: String? = null,
    val totalAmount: Int? = null,
    val balanceAmount: Int? = null
)