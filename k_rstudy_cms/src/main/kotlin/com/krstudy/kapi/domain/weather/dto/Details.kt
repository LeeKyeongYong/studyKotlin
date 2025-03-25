package com.krstudy.kapi.domain.weather.dto

// 강수 확률과 강수량을 포함하는 부가 정보
data class Details(
    val probability: Int, // 강수 확률 (%)
    val amount6Hour: Double, // 6시간 예상 강수량
    val amount12Hour: Double, // 12시간 예상 강수량
    val snowAmount6Hour: Double, // 6시간 예상 적설량
    val snowAmount12Hour: Double // 12시간 예상 적설량
)