package com.krstudy.kapi.domain.payments.dto

import com.krstudy.kapi.domain.payments.status.PaymentStatus
import java.time.LocalDate

data class PaymentSearchCondition(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val status: PaymentStatus? = null,
    val memberName: String? = null,
    val orderId: String? = null
)