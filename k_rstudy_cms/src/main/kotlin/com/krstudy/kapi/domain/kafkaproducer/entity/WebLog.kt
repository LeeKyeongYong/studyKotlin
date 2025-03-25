package com.krstudy.kapi.domain.kafkaproducer.entity

import lombok.Data

@Data
class WebLog(
    var userId: String = "",
    var url: String = "",
    var timestamp: String = "",
    var sessionId: String = "",
    var ipAddress: String = ""
)