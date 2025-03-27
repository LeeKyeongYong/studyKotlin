package com.study.nofitication

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import org.springframework.http.MediaType
@RestController
@RequestMapping("/notifications")
class NotificationController {

    private val notificationsSink = Sinks.many().multicast().onBackpressureBuffer<Notification>()

    @GetMapping("/send")
    fun publishNotification(@RequestParam message: String): String {
        val notification = Notification(message)
        notificationsSink.tryEmitNext(notification)
        return "Notification sent: $message"
    }

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamNotification(): Flux<Notification> {
        return notificationsSink.asFlux()
    }
}