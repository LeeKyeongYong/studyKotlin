package com.krstudy.kapi.domain.excel.entity

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.web.servlet.view.document.AbstractXlsView
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URLEncoder

class CustomExcelView : AbstractXlsView() {

    override fun buildExcelDocument(model: Map<String, Any>, workbook: Workbook, request: HttpServletRequest, response: HttpServletResponse) {
        // 셀 타입에 따라 적절히 처리
        val sheet = workbook.createSheet("Data")
        val row = sheet.createRow(0)
        val cell = row.createCell(0)
        cell.setCellValue("Example")

        // HTTP 응답 헤더 설정
        val filename = URLEncoder.encode("회원_데이터.xls", "UTF-8").replace("+", "%20")
        response.setHeader("Content-Disposition", "attachment; filename=\"$filename\"")
    }

    private fun getCellValue(cell: Cell): Any? {
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue
            CellType.BOOLEAN -> cell.booleanCellValue
            CellType.FORMULA -> cell.cellFormula
            else -> null
        }
    }
}
