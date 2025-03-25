package com.krstudy.kapi.global.init

import com.krstudy.kapi.domain.member.service.MemberService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException

@Configuration
class AllInit(private val memberService: MemberService) {
    private val log = LoggerFactory.getLogger(AllInit::class.java)

    @Bean
    @Order(2)
    fun initAll(): ApplicationRunner {
        val defaultImageBytes = getDefaultImageBytes() // 기본 이미지 바이트 가져오기

        return ApplicationRunner {
            CoroutineScope(Dispatchers.Default).launch {
                if (memberService.findByUserid("system") == null) {
                    memberService.join("system", "시스템관리자", "시스템관리자", "system@example.com", "1234", "image/jpeg", defaultImageBytes, "","","WEB")
                    memberService.join("admin", "관리자","관리자", "admin@example.com", "1234", "image/jpeg", defaultImageBytes, "","","")
                }
            }
        }
    }

    private fun getDefaultImageBytes(): ByteArray {
        val inputStream = this::class.java.classLoader.getResourceAsStream("gen/images/notphoto.jpg")
            ?: throw FileNotFoundException("Default image not found in resources")

        return inputStream.readBytes().also {
            inputStream.close() // InputStream을 닫기
        }
    }
}