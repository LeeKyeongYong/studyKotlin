package com.krstudy.kapi.domain.payments.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.payments.dto.PaymentResponseDto
import com.krstudy.kapi.domain.payments.status.CashReceiptStatus
import com.krstudy.kapi.domain.payments.status.PaymentStatus
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cash_receipts")
class CashReceipt(
    @Column(length = 200, unique = true)
    val receiptKey: String,

    @Column(nullable = false)
    val orderId: String,

    @Column(nullable = false)
    val orderName: String,

    @Column(nullable = false)
    val type: String,

    @Column(length = 9)
    val issueNumber: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var issueStatus: CashReceiptStatus,

    @Column(nullable = false)
    val amount: Int,

    @Column
    val taxFreeAmount: Int?,

    @Column(nullable = false)
    val customerIdentityNumber: String,

    @Column
    val receiptUrl: String?,

    @Column(length = 10)
    val businessNumber: String?,

    @Column(nullable = false)
    val transactionType: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    val payment: Payment? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    @Column(nullable = false)
    val requestedAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity()
