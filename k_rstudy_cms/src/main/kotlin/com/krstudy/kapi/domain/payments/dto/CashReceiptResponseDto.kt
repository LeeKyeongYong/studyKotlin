package com.krstudy.kapi.domain.payments.dto

data class CashReceiptResponseDto(
    val receiptKey: String,
    val orderId: String? = null,
    val orderName: String? = null,
    val type: String? = null,
    val issueNumber: String? = null,
    val receiptUrl: String? = null,
    val businessNumber: String? = null,
    val transactionType: String? = null,
    val amount: Int? = null,
    val taxFreeAmount: Int? = null,
    val issueStatus: String? = null,
    val failure: FailureDto? = null,
    val requestedAt: String? = null,
    val customerIdentityNumber: String? = null,
    val message: String? = null
) {
    companion object {
        // 실패 응답을 위한 팩토리 메서드
        fun failure(receiptKey: String, message: String) = CashReceiptResponseDto(
            receiptKey = receiptKey,
            message = message
        )

        // 성공 응답을 위한 팩토리 메서드
        fun success(
            receiptKey: String,
            receiptUrl: String?,
            message: String,
            issueStatus: String
        ) = CashReceiptResponseDto(
            receiptKey = receiptKey,
            receiptUrl = receiptUrl,
            message = message,
            issueStatus = issueStatus
        )
    }
}