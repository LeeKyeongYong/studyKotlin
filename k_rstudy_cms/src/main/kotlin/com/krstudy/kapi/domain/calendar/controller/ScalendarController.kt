package com.krstudy.kapi.domain.calendar.controller

import com.krstudy.kapi.domain.calendar.entity.Scalendar
import com.krstudy.kapi.domain.calendar.service.ScalendarService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class ScalendarController(
    private val scalendarService: ScalendarService
) {


    // HTML 페이지를 특정 URL 경로로 제공
    @RequestMapping("/calendar")
    fun calendarPage(): String {
        return "domain/calendar/calendar"  // src/main/resources/templates/calendar.html을 반환
    }

    @RequestMapping("/calendar/write")
    fun writePage(): String {
        return "domain/calendar/write"  // src/main/resources/templates/write.html을 반환
    }

    @RequestMapping("/calendar/modify")
    fun modifyPage(): String {//수정기능
        return "domain/calendar/modify"  // src/main/resources/templates/modify.html을 반환
    }

    @RequestMapping("/calendar/view")
    fun calendarViewPage(): String {
        return "domain/calendar/calendar_view"  // src/main/resources/templates/calendar_view.html을 반환
    }

}
