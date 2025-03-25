package com.krstudy.kapi.global.app

import com.krstudy.kapi.domain.monitoring.service.SystemMonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
class SchedulerConfig @Autowired constructor(
    private val systemMonitorService: SystemMonitorService
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    @Scheduled(fixedRate = 5000)
    fun collectMetrics() {
        coroutineScope.launch {
            systemMonitorService.collectSystemMetrics()
        }
    }
}