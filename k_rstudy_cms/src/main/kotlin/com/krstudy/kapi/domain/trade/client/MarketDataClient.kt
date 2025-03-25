package com.krstudy.kapi.com.krstudy.kapi.domain.trade.client

import com.krstudy.kapi.domain.trade.dto.HogaDto

interface MarketDataClient {
    suspend fun getHogaInfo(coinCode: String): HogaDto
}