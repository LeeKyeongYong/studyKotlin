package com.krstudy.kapi.global.app


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextListener
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CustomWebMvcConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/v1/**")  // v1 경로에 대한 CORS 설정
            .allowedOrigins("http://localhost:8090")  // 개발 서버 URL 추가
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)

        registry.addMapping("/api/**")  // api 경로에 대한 CORS 설정
            .allowedOrigins("http://localhost:8090")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)

        registry.addMapping("/api/**")  // api 경로에 대한 CORS 설정
            .allowedOrigins("https://cdpn.io")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/gen/**")
            .addResourceLocations("classpath:/gen/", "/gen/")

        // 추가할 /image/** 리소스 핸들러
        registry.addResourceHandler("/image/**")
            .addResourceLocations("classpath:/static/image/")
            .setCachePeriod(3600) // 캐시 설정 (옵션)

    }

}