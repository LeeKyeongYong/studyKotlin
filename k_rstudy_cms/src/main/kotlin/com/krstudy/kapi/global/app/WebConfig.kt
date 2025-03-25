package com.krstudy.kapi.global.app

import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.context.request.RequestContextListener
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.util.concurrent.TimeUnit
import org.springframework.web.servlet.resource.VersionResourceResolver
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.config.web.server.ServerHttpSecurity

@Configuration
class WebConfig(
    @Value("\${spring.application.version}") private val appVersion: String
) : WebMvcConfigurer {

    @Bean
    fun requestContextListener(): RequestContextListener {
        return RequestContextListener()
    }


    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer
            .defaultContentType(MediaType.TEXT_HTML)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("js", MediaType("application", "javascript"))
            .mediaType("css", MediaType("text", "css"))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 정적 리소스 핸들러
        registry.addResourceHandler("/resource/**")
            .addResourceLocations(
                "classpath:/static/resource/",
                "https://cdn.jsdelivr.net/",
                "https://cdnjs.cloudflare.com/"
            )
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .resourceChain(true)
            .addResolver(VersionResourceResolver().addContentVersionStrategy("/**"))
            .addResolver(PathResourceResolver())

        // 정적 파일들
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))

        // JS 파일들 (패턴 수정)
        registry.addResourceHandler("/js/**")
            .addResourceLocations(
                "classpath:/static/js/",
                "classpath:/templates/js/"
            )
            .setCacheControl(CacheControl.noCache())

        // 모니터링
        registry.addResourceHandler("/monitoring/**")
            .addResourceLocations("classpath:/templates/domain/monitoring/")
            .setCacheControl(CacheControl.noCache())
    }

    @Bean
    fun resourceVersioning(): String = appVersion
}



//    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
//        configurer
//            .defaultContentType(MediaType.TEXT_HTML)
//            .mediaType("html", MediaType.TEXT_HTML)
//            .mediaType("json", MediaType.APPLICATION_JSON)
//            .mediaType("js", MediaType("application", "javascript"))
//            .mediaType("javascript", MediaType("application", "javascript"))
//    }
//
//    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
//        registry.addResourceHandler("/resource/**")
//            .addResourceLocations("classpath:/static/resource/")
//            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
//            .resourceChain(true)
//            .addResolver(VersionResourceResolver().addContentVersionStrategy("/**"))
//            .addResolver(PathResourceResolver())
//
//        registry.addResourceHandler("/static/**")
//            .addResourceLocations("classpath:/static/")
//            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
//            .resourceChain(true)
//            .addResolver(PathResourceResolver())
//
//        registry.addResourceHandler("/images/**")
//            .addResourceLocations("file:gen/images/", "classpath:/static/images/")
//            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
//            .resourceChain(true)
//            .addResolver(PathResourceResolver())
//
//        registry.addResourceHandler("/templates/**")
//            .addResourceLocations("classpath:/templates/")
//            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
//            .resourceChain(true)
//            .addResolver(PathResourceResolver())
//
//        registry.addResourceHandler("/**/*.js")
//            .addResourceLocations("classpath:/static/", "classpath:/templates/")
//            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
//            .resourceChain(true)
//            .addResolver(PathResourceResolver())
//
//        registry.addResourceHandler("/favicon.ico")
//            .addResourceLocations("classpath:/static/")
//            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
//            .resourceChain(true)
//            .addResolver(PathResourceResolver())
//
//        registry.addResourceHandler("/monitoring/**")
//            .addResourceLocations("classpath:/templates/domain/monitoring/")
//            .setCacheControl(CacheControl.noCache())
//            .resourceChain(true)
//            .addResolver(PathResourceResolver())
//
//    }