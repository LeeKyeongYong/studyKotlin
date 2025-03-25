package com.krstudy.kapi.emails.repository

import com.krstudy.kapi.domain.emails.entity.VerificationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
interface VerificationCodeRepository : JpaRepository<VerificationCode, String> {
    fun findByCode(code: String): VerificationCode? // 추가된 메서드
}