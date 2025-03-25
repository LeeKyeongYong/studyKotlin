package com.krstudy.kapi.domain.payments.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.domain.payments.dto.*
import com.krstudy.kapi.domain.payments.entity.CashReceipt
import com.krstudy.kapi.domain.payments.entity.Payment
import com.krstudy.kapi.domain.payments.entity.PaymentCancel
import com.krstudy.kapi.domain.payments.repository.CashReceiptRepository
import com.krstudy.kapi.domain.payments.repository.PaymentRepository
import com.krstudy.kapi.domain.payments.status.CashReceiptStatus
import com.krstudy.kapi.domain.payments.status.PaymentStatus
import net.minidev.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.util.Base64
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class PaymentService(
    private val cashReceiptRepository: CashReceiptRepository,
    private val paymentRepository: PaymentRepository,
    private val memberService: MemberService,
    @Value("\${api.key}") private val secretKey: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val tosUrl="https://api.tosspayments.com/v1"
    private val tossPaymentUrl = tosUrl+"/payments"
    private val tossCashReceiptUrl = tosUrl+"/cash-receipts"


    fun confirmPayment(request: PaymentRequestDto, author: Member?): PaymentResponseDto {
        try {

            // 2. 토스 페이먼츠 API 호출
            val tossResponse = callTossPaymentApi(request)

            // 3. 결제 정보 저장
            val payment = Payment(
                orderId = request.orderId,
                amount = request.amount,
                paymentKey = request.paymentKey,
                status = PaymentStatus.COMPLETED,
                member = author,
                completedAt = LocalDateTime.now(),
                remainingAmount = request.amount // remainingAmount 추가
            )

            val savedPayment = paymentRepository.save(payment)
            return PaymentResponseDto(
                orderId = savedPayment.orderId,
                amount = savedPayment.amount,
                status = savedPayment.status,
                paymentKey = savedPayment.paymentKey,
                completedAt = savedPayment.completedAt
            )
        } catch (e: Exception) {
            logger.error("Payment processing error", e)
            when (e) {
                is GlobalException -> throw e
                else -> {
                    // 실패한 결제 정보 저장
                    saveFailedPayment(request, author)
                    throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
                }
            }
        }
    }

    private fun saveFailedPayment(request: PaymentRequestDto, author: Member?) {
        try {
            val failedPayment = Payment(
                orderId = request.orderId,
                amount = request.amount,
                paymentKey = request.paymentKey,
                status = PaymentStatus.FAILED,
                member = author,
                completedAt = LocalDateTime.now(),
                remainingAmount = 0 // 실패한 결제는 잔액을 0으로 설정
            )
            paymentRepository.save(failedPayment)
        } catch (e: Exception) {
            logger.error("Failed to save failed payment", e)
        }
    }

    private fun callTossPaymentApi(request: PaymentRequestDto): TossPaymentResponse {
        val client = HttpClient.newBuilder().build()

        val requestBody = JSONObject().apply {
            put("paymentKey", request.paymentKey)
            put("orderId", request.orderId)
            put("amount", request.amount)
        }

        logger.debug("Sending request to Toss API: $requestBody")

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI(tossPaymentUrl+"/confirm"))
            .header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$secretKey:".toByteArray())}")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build()

        return try {
            val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            logger.debug("Toss API Response - Status: ${response.statusCode()}, Body: ${response.body()}")

            when (response.statusCode()) {
                200 -> {
                    val mapper = ObjectMapper().apply {
                        // JSON 처리 시 알 수 없는 속성 무시
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        // null 값을 기본값으로 처리
                        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                        // 빈 객체 허용
                        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                    }
                    mapper.readValue(response.body(), TossPaymentResponse::class.java)
                }
                401 -> throw GlobalException(MessageCode.UNAUTHORIZED)
                else -> {
                    logger.error("Toss payment failed with status: ${response.statusCode()}, body: ${response.body()}")
                    throw GlobalException(MessageCode.PAYMENT_FAILED)
                }
            }
        } catch (e: Exception) {
            logger.error("Toss API call failed", e)
            when (e) {
                is GlobalException -> throw e
                else -> throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
            }
        }
    }

    // 결제 조회
    @Transactional(readOnly = true)
    fun getPaymentByOrderId(orderId: String): Payment? {
        return paymentRepository.findByOrderId(orderId)
    }

    // 사용자의 결제 내역 조회
    @Transactional(readOnly = true)
    fun getPaymentsByMember(memberId: Long): List<Payment> {
        return paymentRepository.findByMemberId(memberId)
    }

    @Transactional(readOnly = true)
    fun getPaymentByPaymentKey(paymentKey: String): Payment? {
        return paymentRepository.findByPaymentKey(paymentKey)
    }

    @Transactional
    fun cancelPayment(request: PaymentCancelRequestDto): PaymentCancelResponseDto {
        // 1. 결제 정보 조회
        val payment = paymentRepository.findByPaymentKey(request.paymentKey)
            ?: throw GlobalException(MessageCode.PAYMENT_NOT_FOUND)

        // 2. 취소 가능 여부 확인
        if (!payment.status.isCancelable()) {
            throw GlobalException(MessageCode.PAYMENT_NOT_CANCELABLE)
        }

        // 3. 취소 금액 검증
        val cancelAmount = request.cancelAmount ?: payment.remainingAmount
        if (cancelAmount > payment.remainingAmount) {
            throw GlobalException(MessageCode.INVALID_CANCEL_AMOUNT)
        }

        // 4. 토스 페이먼츠 API 호출
        val tossResponse = callTossCancelApi(request)

        // 5. 취소 정보 저장
        val paymentCancel = PaymentCancel(
            payment = payment,
            cancelReason = request.cancelReason,
            cancelAmount = cancelAmount,
            transactionKey = tossResponse.lastTransactionKey ?: throw GlobalException(MessageCode.PAYMENT_CANCEL_FAILED)
        )

        // 6. 결제 상태 및 잔액 업데이트
        payment.remainingAmount -= cancelAmount
        payment.status = if (payment.remainingAmount == 0) {
            PaymentStatus.CANCELED
        } else {
            PaymentStatus.PARTIAL_CANCELED
        }
        payment.cancels.add(paymentCancel)

        // 7. 응답 생성
        return PaymentCancelResponseDto(
            paymentKey = payment.paymentKey,
            orderId = payment.orderId,
            status = payment.status,
            transactionKey = paymentCancel.transactionKey,
            cancelReason = paymentCancel.cancelReason,
            canceledAt = paymentCancel.canceledAt,
            cancelAmount = paymentCancel.cancelAmount,
            remainingAmount = payment.remainingAmount
        )
    }

    private fun callTossCancelApi(request: PaymentCancelRequestDto): TossCancelResponse {
        val client = HttpClient.newBuilder().build()

        val requestBody = JSONObject().apply {
            put("cancelReason", request.cancelReason)
            request.cancelAmount?.let { put("cancelAmount", it) }
            request.taxFreeAmount?.let { put("taxFreeAmount", it) }
            request.refundReceiveAccount?.let {
                put("refundReceiveAccount", mapOf(
                    "bank" to it.bank,
                    "accountNumber" to it.accountNumber,
                    "holderName" to it.holderName
                ))
            }
        }

        logger.debug("Sending cancel request to Toss API: $requestBody") // 요청 로깅 추가

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI("$tossPaymentUrl/${request.paymentKey}/cancel"))
            .header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$secretKey:".toByteArray())}")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build()

        return try {
            val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            logger.debug("Toss API Response - Status: ${response.statusCode()}, Body: ${response.body()}") // 응답 로깅 추가
            handleTossResponse(response)
        } catch (e: Exception) {
            logger.error("Payment cancel failed", e)
            throw GlobalException(MessageCode.PAYMENT_CANCEL_FAILED)
        }
    }

    private fun handleTossResponse(response: HttpResponse<String>): TossCancelResponse {
        return when (response.statusCode()) {
            200 -> {
                val mapper = ObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                   configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                }

                try {
                    mapper.readValue(response.body(), TossCancelResponse::class.java)
                } catch (e: Exception) {
                    logger.error("Failed to parse Toss response: ${response.body()}", e)
                    throw GlobalException(MessageCode.PAYMENT_CANCEL_FAILED)
                }
            }
            401 -> throw GlobalException(MessageCode.UNAUTHORIZED)
            else -> {
                logger.error("Toss payment cancel failed with status: ${response.statusCode()}, body: ${response.body()}")
                throw GlobalException(MessageCode.PAYMENT_CANCEL_FAILED)
            }
        }
    }

    @Transactional
    fun issueCashReceipt(request: CashReceiptRequestDto): CashReceiptResponseDto {
        logger.info("현금영수증 발급 시작 - 주문번호: ${request.orderId}")

        try {
            // 1. 이미 발급된 영수증이 있는지 확인
            val existingReceipt = cashReceiptRepository.findByOrderId(request.orderId)
            if (existingReceipt?.issueStatus == CashReceiptStatus.COMPLETED) {
                return CashReceiptResponseDto.success(
                    receiptKey = existingReceipt.receiptKey,
                    receiptUrl = existingReceipt.receiptUrl,
                    message = "이미 발급된 현금영수증이 있습니다.",
                    issueStatus = CashReceiptStatus.COMPLETED.name
                )
            }

            // 2. 토스 페이먼츠 API 호출
            val response = try {
                callTossCashReceiptApi(request)
            } catch (e: Exception) {
                logger.error("토스 API 호출 실패", e)
                null
            }

            // 3. 현금영수증 정보 저장 (성공/실패 모두 저장)
            val cashReceipt = CashReceipt(
                receiptKey = response?.receiptKey ?: UUID.randomUUID().toString(),
                orderId = request.orderId,
                orderName = request.orderName,
                type = request.type,
                issueNumber = response?.issueNumber,
                issueStatus = if (response != null) CashReceiptStatus.COMPLETED else CashReceiptStatus.FAILED,
                amount = request.amount,
                taxFreeAmount = request.taxFreeAmount,
                customerIdentityNumber = request.customerIdentityNumber,
                receiptUrl = response?.receiptUrl,
                businessNumber = response?.businessNumber,
                transactionType = response?.transactionType ?: "CONFIRM"
            )

            val savedReceipt = cashReceiptRepository.save(cashReceipt)
            logger.info("현금영수증 정보 저장 완료 - ID: ${savedReceipt.id}, 상태: ${savedReceipt.issueStatus}")

            // 4. 응답 반환
            return if (response != null) {
                CashReceiptResponseDto.success(
                    receiptKey = savedReceipt.receiptKey,
                    receiptUrl = savedReceipt.receiptUrl,
                    message = "현금영수증이 정상적으로 발급되었습니다.",
                    issueStatus = CashReceiptStatus.COMPLETED.name
                )
            } else {
                CashReceiptResponseDto.failure(
                    receiptKey = savedReceipt.receiptKey,
                    message = "현금영수증 발급에 실패했습니다."
                )
            }

        } catch (e: Exception) {
            logger.error("현금영수증 발급 중 오류 발생", e)
            throw GlobalException(MessageCode.CASH_RECEIPT_ISSUANCE_FAILED)
        }
    }

    private fun callTossCashReceiptApi(request: CashReceiptRequestDto): CashReceiptResponseDto {
        val client = HttpClient.newBuilder().build()

        val requestBody = JSONObject().apply {
            put("amount", request.amount)
            put("orderId", request.orderId)
            put("orderName", request.orderName)
            put("customerIdentityNumber", request.customerIdentityNumber)
            put("type", request.type)
            request.taxFreeAmount?.let { put("taxFreeAmount", it) }
        }

        // Base64 인코딩 수정
        val encodedKey = Base64.getEncoder().encodeToString("${secretKey}:".toByteArray(Charsets.UTF_8))

        logger.debug("header API: Basic ${encodedKey}")
        // 요청 로깅 추가
        logger.debug("Request to Toss API: ${requestBody.toString()}")

        //.uri(URI(tossCashReceiptUrl))
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI("https://api.tosspayments.com/v1/cash-receipts"))
            .header("Authorization", "Basic $encodedKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build()

        return try {
            val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            // 응답 로깅 추가
            logger.debug("Toss API Response - Status: ${response.statusCode()}, Body: ${response.body()}")

            when (response.statusCode()) {
                200 -> {
                    val mapper = ObjectMapper().apply {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }
                    mapper.readValue(response.body(), CashReceiptResponseDto::class.java)
                }
                401 -> {
                    logger.error("Authentication failed with Toss API: ${response.body()}")
                    throw GlobalException(MessageCode.UNAUTHORIZED)
                }
                else -> {
                    logger.error("Cash receipt issuance failed: ${response.body()}")
                    throw GlobalException(MessageCode.CASH_RECEIPT_ISSUANCE_FAILED)
                }
            }
        } catch (e: Exception) {
            logger.error("Toss API call failed", e)
            throw GlobalException(MessageCode.CASH_RECEIPT_ISSUANCE_FAILED)
        }
    }

    private fun cashReceipthandleTossResponse(response: HttpResponse<String>): CashReceiptResponseDto {
        return when (response.statusCode()) {
            200 -> {
                val mapper = ObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
                mapper.readValue(response.body(), CashReceiptResponseDto::class.java)
            }
            else -> {
                logger.error("Cash receipt issuance failed: ${response.body()}")
                throw GlobalException(MessageCode.CASH_RECEIPT_ISSUANCE_FAILED)
            }
        }
    }

    @Transactional(readOnly = true)
    fun getCashReceipts(requestDate: LocalDate): List<CashReceipt> {
        return cashReceiptRepository.findByRequestedAtBetween(
            requestDate.atStartOfDay(),
            requestDate.plusDays(1).atStartOfDay()
        )
    }

    @Transactional(readOnly = true)
    fun getPaymentList(
        memberId: Long?,
        condition: PaymentSearchCondition,
        pageable: Pageable
    ): Page<PaymentListDto> {
        logger.debug("Search Conditions: memberId=$memberId, startDate=${condition.startDate}, " +
                "endDate=${condition.endDate}, status=${condition.status}, " +
                "memberName=${condition.memberName}, orderId=${condition.orderId}")

        val payments = paymentRepository.searchPayments(
            memberId = memberId,
            startDate = condition.startDate?.atStartOfDay(),
            endDate = condition.endDate?.atTime(23, 59, 59),
            status = condition.status,
            memberName = condition.memberName,
            orderId = condition.orderId,
            pageable = pageable
        )

        logger.debug("Found ${payments.totalElements} payments")

        return payments.map { payment ->
            PaymentListDto(
                id = payment.id!!,
                orderId = payment.orderId,
                amount = payment.amount,
                status = payment.status,
                memberName = payment.member?.username ?: "Unknown",
                memberId = payment.member?.userid ?: "Unknown",
                createdAt = payment.createdAt,
                completedAt = payment.completedAt,
                canceledAt = payment.cancels.firstOrNull()?.canceledAt,
                receiptUrl = generateReceiptUrl(payment),
                paymentKey = payment.paymentKey
            )
        }
    }

    private fun generateReceiptUrl(payment: Payment): String {
        return "https://dashboard.tosspayments.com/receipts/${payment.paymentKey}"
    }

}