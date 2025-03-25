package com.krstudy.kapi.domain.calendar.repository

import com.krstudy.kapi.domain.calendar.entity.Scalendar
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScalendarRepository : JpaRepository<Scalendar, Long> {}