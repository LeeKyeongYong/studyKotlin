package com.krstudy.kapi.global.https

import com.krstudy.kapi.global.exception.MessageCode

class RespData<T>(
    val resultCode: String,
    val statusCode: Int,
    val msg: String,
    val data: T?
) {
    companion object {
        fun <T> of(resultCode: String, msg: String, data: T? = null): RespData<T> {
            val statusCode = resultCode.split("-", limit = 2)[0].toIntOrNull() ?: 0
            return RespData(resultCode, statusCode, msg, data)
        }

        fun <T> fromErrorCode(errorCode: MessageCode): RespData<T> {
            return RespData(
                errorCode.code,
                errorCode.code.split("-", limit = 2)[0].toIntOrNull() ?: 0,
                errorCode.message,
                null
            )
        }
    }

    // 새로운 데이터를 가진 RespData 객체를 반환하는 메서드 추가
    fun <U> newDataOf(newData: U): RespData<U> {
        return RespData(resultCode, statusCode, msg, newData)
    }

    fun isSuccess(): Boolean {
        return statusCode in 200 until 400
    }

    fun isFail(): Boolean {
        return !isSuccess()
    }
}