package com.krstudy.kapi.domain.payments.dto

data class TossCancelResponse(
    val mId: String? = null,
    val version: String? = null,
    val lastTransactionKey: String? = null,
    val paymentKey: String? = null,
    val status: String? = null,
    val orderId: String? = null,
    val cancels: List<CancelDetail>? = null
)