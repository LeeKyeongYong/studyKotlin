package com.krstudy.kapi.domain.kafkaproducer.rw

import com.krstudy.kapi.domain.kafkaproducer.entity.WebLog
import net.datafaker.Faker
import org.springframework.batch.item.ItemReader
import java.util.UUID
import java.util.concurrent.TimeUnit

// open 키워드 추가
open class WebLogItemReader : ItemReader<WebLog> {

    private val faker = Faker()
    private var count = 0
    private val maxCount = 100 // 생성할 최대 아이템 수

    // 자주 접속하는 특정 IP
    private val frequentIp = "192.168.1.1" // 특정 IP 주소
    private val frequentIpProbability = 0.7 // 특정 IP가 생성될 확률 (70%)

    @Throws(Exception::class)
    override fun read(): WebLog? {
        return if (count < maxCount) {
            count++
            genNewWebLog()
        } else {
            null // 더 이상 읽을 데이터가 없음을 알림
        }
    }

    private fun genNewWebLog(): WebLog {
        val myWebLog = WebLog()
        // 특정 IP를 70% 확률로 생성하고, 나머지는 랜덤 IP
        myWebLog.ipAddress = if (Math.random() < frequentIpProbability) {
            frequentIp // 특정 IP 설정
        } else {
            faker.internet().ipV4Address() // 랜덤 IP 설정
        }

        myWebLog.url = faker.internet().url()
        return myWebLog
    }
}


/*
    private val faker = Faker()
    private val genCount = 100
    private var currentCount = 0

    override fun read(): WebLog? {
        return if (currentCount <= genCount) {
            currentCount++
            genNewWebLog()
        } else {
            null
        }
    }

    private fun genNewWebLog(): WebLog {
        val webLog = WebLog()
        webLog.apply {
            ipAddress = faker.internet().ipV4Address()
            url = faker.internet().url()
            userId = faker.idNumber().singaporeanFinBefore2000()
            sessionId = UUID.randomUUID().toString()
            timestamp = faker.date().past(2, TimeUnit.DAYS).toString()
        }
        return webLog
    }

 */