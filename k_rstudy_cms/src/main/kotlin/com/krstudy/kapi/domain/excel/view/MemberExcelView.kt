package com.krstudy.kapi.domain.excel.view

import com.krstudy.kapi.domain.excel.entity.MemberView
import com.krstudy.kapi.com.krstudy.kapi.domain.excel.repository.MemberViewRepository
import com.krstudy.kapi.standard.base.AbstractExcelView
import jakarta.servlet.http.HttpServletRequest
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MemberExcelView : AbstractExcelView<MemberView>() {

    @Autowired
    private lateinit var memberViewRepository: MemberViewRepository

    override fun getData(request: HttpServletRequest): List<MemberView> {
        return memberViewRepository.findAll() // 실제 필요한 데이터 조회 로직으로 변경
    }

    override fun fillData(sheet: Sheet, items: List<MemberView>, bodyStyle: CellStyle) {
        items.forEachIndexed { index, member ->
            val row = sheet.createRow(4 + index)
            row.createCell(0).setCellValue(member.memberId?.toDouble() ?: 0.0)
            row.createCell(1).setCellValue(member.userid ?: "")
            row.createCell(2).setCellValue(member.username ?: "")
            row.createCell(3).setCellValue(member.roleType ?: "")
            row.createCell(4).setCellValue(member.isAdmin.toString())
            // 다른 필요한 데이터들도 추가
        }
    }
}