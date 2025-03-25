package com.krstudy.kapi.domain.member.repository

import org.springframework.data.jpa.domain.Specification
import com.krstudy.kapi.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*
import org.springframework.data.jpa.domain.Specification.where

@Repository
interface MemberRepository : JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    fun findByUsername(username: String): Member?
    fun findByUserid(userId: String): Member?
    fun findByJwtToken(jwtToken: String): Member?
    fun findByUsernameContaining(username: String): List<Member>
    fun findByUsernameAndUserEmailAndAccountType(
        username: String,
        userEmail: String,
        accountType: String = "WEB"
    ): Optional<Member>
}