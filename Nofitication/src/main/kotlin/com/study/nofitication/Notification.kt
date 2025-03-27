package com.study.nofitication

import java.time.Instant

data class Notification(
    val message: String,
    val timestamp: Long = Instant.now().toEpochMilli()
)