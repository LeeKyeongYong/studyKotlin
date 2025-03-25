package com.krstudy.kapi.domain.payments.repository

import com.krstudy.kapi.domain.payments.entity.CashReceipt
import com.krstudy.kapi.domain.payments.status.CashReceiptStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CashReceiptRepository : JpaRepository<CashReceipt, Long> {
    fun findByRequestedAtBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): List<CashReceipt>

    fun findByOrderId(orderId: String): CashReceipt?

    fun findByReceiptKey(receiptKey: String): CashReceipt?

    fun existsByOrderIdAndIssueStatus(
        orderId: String,
        issueStatus: CashReceiptStatus
    ): Boolean
}
