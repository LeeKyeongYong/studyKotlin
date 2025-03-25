package com.krstudy.kapi.global.app

import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JasyptConfig {

    @Value("\${jasypt.encryptor.password}")
    private lateinit var passwordKey: String

    @Bean
    fun jasyptStringEncryptor(): StandardPBEStringEncryptor {
        val encryptor = StandardPBEStringEncryptor()
        encryptor.setPassword("your-secret-password") // 비밀번호 설정
        encryptor.setAlgorithm("PBEWithMD5AndDES")
        return encryptor
    }

    @Bean("jasyptStringEncryptor")
    fun stringEncryptor(): StringEncryptor {
        val encryptor = PooledPBEStringEncryptor()
        val config = SimpleStringPBEConfig().apply {
            password = passwordKey
            poolSize = 1
            algorithm = "PBEWithMD5AndDES"
            stringOutputType = "base64"
            keyObtentionIterations = 1000
            setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator")
        }
        encryptor.setConfig(config)
        return encryptor
    }
}
