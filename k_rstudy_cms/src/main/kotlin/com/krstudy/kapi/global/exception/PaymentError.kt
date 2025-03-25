package com.krstudy.kapi.global.exception

sealed class PaymentError(val message: String) {
    class OrderNotFound(message: String) : PaymentError(message)
    class InvalidAmount(message: String) : PaymentError(message)
    class ProcessingError(message: String) : PaymentError(message)
}