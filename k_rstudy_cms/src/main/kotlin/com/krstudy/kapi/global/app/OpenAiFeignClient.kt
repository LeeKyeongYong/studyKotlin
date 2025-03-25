package com.krstudy.kapi.global.app


import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAiFeignClient { //http://localhost:8090/swagger-ui/index.html

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Kotlin item api")
                    .description("코틀린 학습하면서 개발하는 API 서버입니다.")
                    .version("1.0.0")
            )
    }
}
