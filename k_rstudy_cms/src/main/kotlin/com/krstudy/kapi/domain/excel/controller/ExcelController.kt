package com.krstudy.kapi.domain.excel.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/excel")
class ExcelController {
    @GetMapping("")
    fun ExcelMain(): String { //사용자 리스트 엑셀 출략
        return "domain/excel/main"
    }

    @GetMapping("/post")
    fun ExcelPost(): String { //게시글 엑셀 출력
        return "domain/excel/post"
    }

    @GetMapping("/calendar")
    fun ExcelCalendar(): String { //달력 엑셀 출력
        return "domain/excel/calendar"
    }

    @GetMapping("/member")
    fun ExcelMember(): String { //달력 엑셀 출력
        return "domain/excel/member"
    }
}