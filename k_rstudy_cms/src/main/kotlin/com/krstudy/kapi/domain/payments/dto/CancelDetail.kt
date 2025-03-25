package com.krstudy.kapi.domain.payments.dto

data class CancelDetail(
    val cancelAmount: Int? = null,
    val cancelReason: String? = null,
    val taxFreeAmount: Int? = null,
    val taxExemptionAmount: Int? = null,
    val refundableAmount: Int? = null,
    val easyPayDiscountAmount: Int? = null,
    val canceledAt: String? = null,
    val transactionKey: String? = null
)