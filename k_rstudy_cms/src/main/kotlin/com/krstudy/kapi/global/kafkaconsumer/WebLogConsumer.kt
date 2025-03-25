package com.krstudy.kapi.global.kafkaconsumer

import com.krstudy.kapi.domain.kafkaproducer.entity.WebLog
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@KafkaListener(
    topics = ["web_log_topic"],
    groupId = "web-log-group",
    containerFactory = "webLogKafkaListenerContainerFactory"
)
fun consume(webLog: WebLog) {
    println("Received WebLog: $webLog")
}