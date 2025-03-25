package com.krstudy.kapi.domain.emails.repository

import com.krstudy.kapi.domain.emails.entity.Email
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailRepository : JpaRepository<Email, Long>