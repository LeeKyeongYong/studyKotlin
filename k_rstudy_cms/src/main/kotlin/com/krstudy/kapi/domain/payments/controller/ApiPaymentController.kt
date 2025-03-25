package com.krstudy.kapi.domain.payments.controller

import com.krstudy.kapi.domain.payments.dto.*
import com.krstudy.kapi.domain.payments.entity.CashReceipt
import com.krstudy.kapi.domain.payments.service.PaymentService
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import com.krstudy.kapi.global.https.ReqData
import org.springframework.security.core.Authentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/payments")
class PaymentApiController(
    private val paymentService: PaymentService,
    private val rq: ReqData
) {
    @PostMapping("/confirm")
    fun confirmPayment(
        @RequestBody request: PaymentRequestDto
    ): ResponseEntity<PaymentResponseDto> {
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)
        val response = paymentService.confirmPayment(request, member)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/cancel/{paymentKey}")
    fun cancelPayment(
        @PathVariable paymentKey: String,
        @RequestBody request: PaymentCancelRequestDto
    ): ResponseEntity<Any> {
        return try {
            val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)

            // 결제 건에 대한 소유권 확인
            val payment = paymentService.getPaymentByPaymentKey(paymentKey)
                ?: throw GlobalException(MessageCode.PAYMENT_NOT_FOUND)

            if (payment.member?.id != member.id) {
                throw GlobalException(MessageCode.UNAUTHORIZED)
            }

            // paymentKey를 request에 설정
            val updatedRequest = request.copy(paymentKey = paymentKey)
            val response = paymentService.cancelPayment(updatedRequest)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to response,
                "message" to "결제가 성공적으로 취소되었습니다."
            ))
        } catch (e: GlobalException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.errorCode.message,
                "code" to e.errorCode.code
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "error" to "결제 취소 중 오류가 발생했습니다.",
                "message" to (e.message ?: "알 수 없는 오류")
            ))
        }
    }

    @PostMapping("/cash-receipts")
    fun issueCashReceipt(
        @RequestBody request: CashReceiptRequestDto
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val response = paymentService.issueCashReceipt(request)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to response,
                "message" to "현금영수증이 정상적으로 발급되었습니다."
            ))
        } catch (e: GlobalException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.errorCode.message,
                "code" to e.errorCode.code
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "error" to "현금영수증 발급 중 오류가 발생했습니다.",
                "message" to (e.message ?: "알 수 없는 오류")
            ))
        }
    }

    @GetMapping("/cash-receipts")
    fun getCashReceipts(
        @RequestParam requestDate: String
    ): ResponseEntity<List<CashReceipt>> {
        val date = LocalDate.parse(requestDate)
        val receipts = paymentService.getCashReceipts(date)
        return ResponseEntity.ok(receipts)
    }

}