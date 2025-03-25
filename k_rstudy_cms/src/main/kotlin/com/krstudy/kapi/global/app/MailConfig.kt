package com.krstudy.kapi.global.app

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
class MailConfig {

    @Value("\${spring.mail.host}")
    private lateinit var mailServerHost: String

    @Value("\${spring.mail.port}")
    private lateinit var mailServerPort: String

    @Value("\${spring.mail.username}")
    private lateinit var mailServerUsername: String

    @Value("\${spring.mail.password}")
    private lateinit var mailServerPassword: String

    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl().apply {
            host = mailServerHost
            port = mailServerPort.toInt()

            username = mailServerUsername
            password = mailServerPassword

            javaMailProperties.apply {
                put("mail.transport.protocol", "smtp")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.ssl.trust", mailServerHost)
            }
        }
        return mailSender
    }
}
