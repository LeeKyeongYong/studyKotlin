package com.krstudy.kapi.global.WebMvc

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.krstudy.kapi.domain.popups.entity.PopupEntity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class PopupCacheConfig {
    @Bean
    fun popupCache(): Cache<Long, PopupEntity> {
        return Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)  // 10분 후 캐시 만료
            .maximumSize(1000)                       // 최대 1000개 항목 저장
            .recordStats()                           // 캐시 통계 기록 활성화
            .build()
    }
}