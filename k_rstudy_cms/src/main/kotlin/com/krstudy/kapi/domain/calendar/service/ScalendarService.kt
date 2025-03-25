package com.krstudy.kapi.domain.calendar.service

import com.krstudy.kapi.domain.calendar.entity.Scalendar
import com.krstudy.kapi.domain.calendar.repository.ScalendarRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScalendarService @Autowired constructor(
    private val scalendarRepository: ScalendarRepository
) {

    // 새로운 Scalendar를 저장하는 메소드
    fun createScalendar(scalendar: Scalendar): Scalendar {
        println("Saving Scalendar: $scalendar")
        return scalendarRepository.save(scalendar)
    }

    // Scalendar를 ID로 조회하는 메소드
    fun getScalendarById(id: Long): Scalendar? {
        return scalendarRepository.findById(id).orElse(null)
    }

    // Scalendar를 업데이트하는 메소드
    @Transactional
    fun updateScalendar(id: Long, updatedScalendar: Scalendar): Scalendar? {
        return scalendarRepository.findById(id).map { existingScalendar ->
            existingScalendar.apply {
                title = updatedScalendar.title
                body = updatedScalendar.body
                startDay = updatedScalendar.startDay
                endDay = updatedScalendar.endDay
                fcolor = updatedScalendar.fcolor
            }
            scalendarRepository.save(existingScalendar)
        }.orElse(null)
    }

    // Scalendar를 ID로 삭제하는 메소드
    fun deleteScalendar(id: Long) {
        scalendarRepository.deleteById(id)
    }

    // 조회 수를 증가시키는 메소드
    @Transactional
    fun increaseHit(id: Long) {
        scalendarRepository.findById(id).ifPresent { scalendar ->
            scalendar.increaseHit()
            scalendarRepository.save(scalendar)
        }
    }

    // 모든 Scalendar 이벤트를 조회하는 메소드
    fun getAllScalendarEvents(): List<Scalendar> {
        return scalendarRepository.findAll()
    }

}