package com.krstudy.kapi.domain.payments.controller

import com.krstudy.kapi.domain.payments.dto.PaymentSearchCondition
import com.krstudy.kapi.domain.payments.service.PaymentService
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import com.krstudy.kapi.global.https.ReqData
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault

@Controller
@RequestMapping("/payments")
class PaymentController (private val paymentService: PaymentService, private val rq: ReqData) {

    @Value("\${api.clientKey}")
    private lateinit var apiKey: String

    @GetMapping("/checkout")
    fun index(request: HttpServletRequest): String {
        rq.setAttribute("apiKey", apiKey)
        return "domain/payments/checkout"
    }

    @GetMapping("/success")
    fun paymentRequest(        @RequestParam paymentKey: String,
                               @RequestParam orderId: String,
                               @RequestParam amount: String): String {
        rq.setAttribute("paymentKey", paymentKey)
        rq.setAttribute("orderId", orderId)
        rq.setAttribute("amount", amount)
        return "domain/payments/success"
    }

    @GetMapping("/fail")
    fun failPayment( @RequestParam code: String,
                     @RequestParam message: String): String {
        rq.setAttribute("code", (code?: "undefined"))
        rq.setAttribute("message", (message?: "알 수 없는 오류가 발생했습니다."))
        return "domain/payments/fail"
    }

    @GetMapping("/detail/{paymentKey}")
    fun paymentDetail(
        @PathVariable paymentKey: String
    ): String {
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)

        val payment = paymentService.getPaymentByPaymentKey(paymentKey)
            ?: throw GlobalException(MessageCode.PAYMENT_NOT_FOUND)

        // 권한 확인
        if (payment.member?.id != member.id) {
            throw GlobalException(MessageCode.UNAUTHORIZED)
        }

        rq.setAttribute("payment", payment)
        return "domain/payments/detail"
    }

    @GetMapping("/list")
    fun paymentList(
        @ModelAttribute condition: PaymentSearchCondition,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): String {
        val member = rq.getMember()
        val memberId = if (!rq.isAdmin()) member?.id else null

        //logger.debug("Received search condition: $condition")

        val payments = paymentService.getPaymentList(memberId, condition, pageable)
        rq.setAttribute("payments", payments)
        rq.setAttribute("condition", condition)

        return "domain/payments/list"
    }

}