package com.krstudy.kapi.domain.item.ano


import com.krstudy.kapi.domain.item.dto.ItemType
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.lang.annotation.*
import kotlin.reflect.KClass

@Constraint(validatedBy = [ItemTypeValidator::class])
@Documented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ItemTypeValid(
    val message: String = "허용되지 않은 물품 유형입니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ItemTypeValidator : ConstraintValidator<ItemTypeValid, String> {
    override fun isValid(cd: String?, context: ConstraintValidatorContext): Boolean {
        if (cd.isNullOrBlank()) return false // Null 또는 빈 문자열 검사

        return ItemType.values().any { it.hasItemCd(cd) }
    }
}
