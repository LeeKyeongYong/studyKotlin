package com.krstudy.kapi.domain.payments.dto

data class RefundReceiveAccount(
    val bank: String,
    val accountNumber: String,
    val holderName: String
)