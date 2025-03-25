package com.krstudy.kapi.global.app

import com.krstudy.kapi.domain.kafkaproducer.entity.WebLog
import com.krstudy.kapi.domain.kafkaproducer.rw.WebLogItemReader
import com.krstudy.kapi.domain.kafkaproducer.rw.WebLogItemWriter
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@EnableBatchProcessing
class KbatchConfig @Autowired constructor(
    private val jobRepository: JobRepository,
    private val platformTransactionManager: PlatformTransactionManager,
    private val kafkaTemplate: KafkaTemplate<String, WebLog>
){
    // Job
    @Bean
    fun webLogGenAndProduceJob(): Job {
        return JobBuilder("webLogGenAndProduceJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(webLogGenAndProduceStep())
            .build()
    }

    // Step
    @Bean
    fun webLogGenAndProduceStep(): Step {
        return StepBuilder("webLogGenAndProduceStep", jobRepository)
            .chunk<WebLog, WebLog>(10, platformTransactionManager)
            .reader(webLogItemReader())
            .writer(webLogItemWriter())
            .build()
    }

    @Bean
    @StepScope
    fun webLogItemReader() = WebLogItemReader()

    @Bean
    @StepScope
    fun webLogItemWriter() = WebLogItemWriter(kafkaTemplate)

    // reader (WebLog Generation --> faker 임의값을 넣어서 object 타입으로 생성)
    // write (생성된 webLog를 Kafka 에 Produce)
}