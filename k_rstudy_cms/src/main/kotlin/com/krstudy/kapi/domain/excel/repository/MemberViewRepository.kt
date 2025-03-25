package com.krstudy.kapi.com.krstudy.kapi.domain.excel.repository

import com.krstudy.kapi.domain.excel.entity.MemberView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberViewRepository : JpaRepository<MemberView, Long> {}
