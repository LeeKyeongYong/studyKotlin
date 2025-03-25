package com.krstudy.kapi.standard.base

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ImageValidator::class])
annotation class ValidImage(
    val message: String = "유효하지 않은 이미지입니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val maxSize: Long = 100 * 1024 * 1024,
    val types: Array<String> = ["image/jpeg", "image/png", "image/gif"]
)