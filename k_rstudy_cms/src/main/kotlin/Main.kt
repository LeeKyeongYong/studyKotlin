package com.krstudy.kapi

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary
import org.springframework.core.task.TaskExecutor
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.data.envers.repository.config.EnableEnversRepositories

@SpringBootApplication
@EnableJpaAuditing
@EnableEnversRepositories  // 수정된 부분
class Main {

    @Bean
    @Primary
    fun taskExecutor(): TaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 10
        taskExecutor.maxPoolSize = 20
        taskExecutor.setThreadNamePrefix("batch-thread-")
        taskExecutor.initialize()
        return taskExecutor
    }

    @Bean
    fun allowCircularReferences(): SpringApplication {
        return SpringApplication().apply {
            setDefaultProperties(mapOf("spring.main.allow-circular-references" to "true"))
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Main::class.java, *args)
}