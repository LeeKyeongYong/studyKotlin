package com.krstudy.kapi.global.lgexecution


import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAspect {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Around("@annotation(logExecutionTime)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint, logExecutionTime: LogExecutionTime): Any? {
        val start = System.currentTimeMillis()
        val proceed = joinPoint.proceed()
        val executionTime = System.currentTimeMillis() - start
        logger.info("${joinPoint.signature} executed in $executionTime ms")
        return proceed
    }
}
