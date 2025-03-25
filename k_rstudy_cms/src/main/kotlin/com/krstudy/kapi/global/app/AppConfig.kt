package com.krstudy.kapi.global.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.itextpdf.text.log.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import jakarta.annotation.PostConstruct
import java.util.logging.Logger

@Configuration
class AppConfig {


    @Value("\${custom.jwt.secretKey}")
    lateinit var jwtSecretKey: String

    @Value("\${custom.accessToken.expirationSec}")
    var accessTokenExpirationSec: Long = 0

    @Value("\${custom.site.backUrl}")
    lateinit var siteBackUrl: String

    @Value("\${custom.site.cookieDomain}")
    lateinit var siteCookieDomain: String

    @Value("\${custom.temp.dirPath}")
    lateinit var tempDirPath: String

    @Value("\${custom.genFile.dirPath}")
    lateinit var genFileDirPath: String

    @Value("\${custom.site.name}")
    lateinit var siteName: String

    @Value("\${custom.dev.backUrl}")
    lateinit var siteFrontUrl: String

    @Value("\${custom.base.pageSize:10}")
    var basePageSize: Int = 10

    @PostConstruct
    fun init() {
        instance = this
    }

    @Bean
    fun jwtSecretKey(): String = jwtSecretKey

    @Bean
    fun accessTokenExpirationSec(): Long = accessTokenExpirationSec

    @Bean
    fun siteCookieDomain(): String = siteCookieDomain

    @Bean
    fun tempDirPath(): String = tempDirPath

    @Bean
    fun genFileDirPath(): String = genFileDirPath

    @Bean
    fun siteName(): String = siteName

    @Bean
    fun siteFrontUrl(): String = siteFrontUrl

    @Bean
    fun basePageSize(): Int = basePageSize

    companion object {
        private lateinit var instance: AppConfig

        fun getResourcesStaticDirPath(): String {
            val resource = ClassPathResource("static/")
            return try {
                resource.file.absolutePath
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        fun getSiteCookieDomain(): String = instance.siteCookieDomain
        fun getSiteFrontUrl(): String = instance.siteFrontUrl
    }
}