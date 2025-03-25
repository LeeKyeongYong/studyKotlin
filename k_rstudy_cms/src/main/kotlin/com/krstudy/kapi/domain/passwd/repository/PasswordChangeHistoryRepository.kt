package com.krstudy.kapi.domain.passwd.repository

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.passwd.entity.PasswordChangeHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PasswordChangeHistoryRepository : JpaRepository<PasswordChangeHistory, Long> {
    fun findByMemberOrderByChangedAtDesc(member: Member): List<PasswordChangeHistory>
    fun findByMemberIdOrderByChangedAtDesc(memberId: Long): List<PasswordChangeHistory>
}