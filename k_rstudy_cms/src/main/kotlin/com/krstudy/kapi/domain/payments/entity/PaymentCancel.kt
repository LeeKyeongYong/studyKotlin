package com.krstudy.kapi.domain.payments.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "payment_cancels")
class PaymentCancel(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    val payment: Payment,

    @Column(nullable = false)
    val cancelReason: String,

    @Column(nullable = false)
    val cancelAmount: Int,

    @Column(nullable = false)
    val transactionKey: String,

    @Column(nullable = false)
    val canceledAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity() {
    init {
        payment.cancels.add(this)
    }
}