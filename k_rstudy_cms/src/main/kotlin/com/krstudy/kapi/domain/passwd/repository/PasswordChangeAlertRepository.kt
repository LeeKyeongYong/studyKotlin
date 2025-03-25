package com.krstudy.kapi.domain.passwd.repository

import com.krstudy.kapi.domain.passwd.entity.PasswordChangeAlert
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PasswordChangeAlertRepository : JpaRepository<PasswordChangeAlert, Long> {
    fun findByMemberId(memberId: Long): PasswordChangeAlert?
}