package com.krstudy.kapi.domain.excel.controller

import com.krstudy.kapi.domain.excel.dto.createModelAndView
import com.krstudy.kapi.domain.excel.view.MemberExcelView
import com.krstudy.kapi.domain.excel.view.PostExcelView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
@RequestMapping("/v1/excel")
class RestExcelController {

    @Autowired
    private lateinit var memberExcelView: MemberExcelView

    @Autowired
    private lateinit var postExcelView: PostExcelView

    @GetMapping("/download/member")
    fun downloadMemberExcel(
        @RequestParam("startDate") startDate: String,
        @RequestParam("endDate") endDate: String,
        @RequestParam("sheetName", defaultValue = "MemberData") sheetName: String,
        @RequestParam("fileName", defaultValue = "회원_데이터") fileName: String
    ): ModelAndView {
        return createModelAndView(
            view = memberExcelView,
            title = "회원 목록",
            headerTitles = listOf("회원ID", "사용자ID", "이름", "역할", "관리자 여부"),
            startDate = startDate,
            endDate = endDate,
            sheetName = sheetName,
            fileName = "$fileName"
        )
    }


    @GetMapping("/download/post")
    fun downloadPostExcel(
        @RequestParam("startDate") startDate: String,
        @RequestParam("endDate") endDate: String,
        @RequestParam("sheetName", defaultValue = "PostData") sheetName: String,
        @RequestParam("fileName", defaultValue = "게시물_데이터") fileName: String
    ): ModelAndView {
        return createModelAndView(
            view = postExcelView,
            title = "게시글 목록",
            headerTitles = listOf("게시물ID", "제목", "작성일", "수정일", "조회수", "작성자ID", "댓글ID", "댓글작성자ID", "댓글작성일", "좋아요ID", "좋아요일"),
            startDate = startDate,
            endDate = endDate,
            sheetName = sheetName,
            fileName = "$fileName"
        )
    }

}
