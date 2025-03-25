package com.krstudy.kapi.domain.emails.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.Entity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import lombok.AccessLevel
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.NoArgsConstructor

@Entity // JPA 엔티티로 지정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
data class VerificationCode(
    var code: String,
    var expirationTimeInMinutes: Int
): BaseEntity()  {
    fun isExpired(verifiedAt: LocalDateTime): Boolean {
        val expiredAt = getCreateDate()?.plusMinutes(expirationTimeInMinutes.toLong())
        return verifiedAt.isAfter(expiredAt)
    }

    fun generateCodeMessage(): String {
        val formattedExpiredAt = (getCreateDate() ?: LocalDateTime.now())
            ?.plusMinutes(expirationTimeInMinutes.toLong())
            ?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: ""

        return """
            [Verification Code]
            $code
            Expired At : $formattedExpiredAt
        """.trimIndent()
    }
}