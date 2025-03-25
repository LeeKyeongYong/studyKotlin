package com.krstudy.kapi.domain.passwd.repository

import com.krstudy.kapi.domain.passwd.entity.MemberSignature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberSignatureRepository : JpaRepository<MemberSignature, Long> {
    fun findByMemberId(memberId: Long): MemberSignature?
}