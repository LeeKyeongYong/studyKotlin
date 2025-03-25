package com.krstudy.kapi.global.app

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled



@Configuration
@EnableScheduling
class KbatchScheduler @Autowired constructor(
    private val webLogGenAndProduceJob: Job,
    private val jobLauncher: JobLauncher
) {
    @Scheduled(fixedDelay = 10000)
    fun runBatchJob() {
        jobLauncher.run(
            webLogGenAndProduceJob,
            JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters()
        )
    }
}