package com.krstudy.kapi.global.exception

import com.krstudy.kapi.global.https.RespData
import com.krstudy.kapi.standard.base.Empty

open class GlobalException(
    val errorCode: MessageCode
) : RuntimeException("${errorCode.code} ${errorCode.message}") {
    var rsData: RespData<Empty> = RespData.of(errorCode.code, errorCode.message) // var로 변경

    constructor(msg: String) : this(MessageCode.BAD_REQUEST) {
        this.rsData = RespData.of("400-0", msg)
    }

    constructor(resultCode: String, msg: String) : this(MessageCode.BAD_REQUEST) {
        this.rsData = RespData.of(resultCode, msg)
    }

    class E404 : GlobalException(MessageCode.NOT_FOUND_RESOURCE)
}