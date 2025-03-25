package com.krstudy.kapi.domain.payments.status


enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELED,
    READY,
    IN_PROGRESS,
    PARTIAL_CANCELED,
    EXPIRED;

    fun displayName(): String {
        return when (this) {
            PENDING -> "결제 대기"
            COMPLETED -> "결제 완료"
            FAILED -> "결제 실패"
            CANCELED -> "결제 취소"
            READY -> "준비"
            IN_PROGRESS -> "진행 중"
            PARTIAL_CANCELED -> "부분 취소"
            EXPIRED -> "만료됨"
        }
    }

    fun isCancelable(): Boolean {
        return this == COMPLETED || this == PARTIAL_CANCELED
    }
}