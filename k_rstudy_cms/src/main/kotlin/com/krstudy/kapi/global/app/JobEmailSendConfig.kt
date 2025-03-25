package com.krstudy.kapi.com.krstudy.kapi.global.app

import com.krstudy.kapi.domain.weather.service.WeatherService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class JobEmailSendConfig(
    private val weatherService: WeatherService
//    private val resolver: JobPostingResolver,
//    private val jobPostingService: JobPostingService,
//    private val emailSendingService: EmailSendingService
) {

    private val log = LoggerFactory.getLogger(JobEmailSendConfig::class.java)

    /**
     * 실행하면 메세지를 보낸다. 수동으로 크롤링을 수행하기 위해 필요함
     */
    @PostConstruct
    fun init() {
        executeRegularUpdate()
    }

    @Async
    //@Scheduled(cron = "0 30 18 * * ?")
    @Scheduled(cron = "0 0 0/3 * * ?")
    //@Scheduled(cron = "0 */1 * * * ?")
    fun executeRegularUpdate() {
        log.info("{}일자 스케쥴러를 실행합니다.", LocalDate.now())
        changeServer()
        initializeWeatherData()
//        val postings = jobPostingService.getNewlyJobPosting()
//        emailSendingService.sendJobPostings(postings)
    }

    private fun changeServer() {
//        val crawlingResults = resolver.crawling()
//        jobPostingService.updateAll(crawlingResults)
    }

    fun initializeWeatherData(){
        weatherService.updateWeatherData(59,125)
    }
}