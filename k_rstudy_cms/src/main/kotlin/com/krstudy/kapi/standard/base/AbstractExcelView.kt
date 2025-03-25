package com.krstudy.kapi.standard.base

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.*
import org.springframework.web.servlet.view.document.AbstractXlsView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 엑셀 파일을 생성하기 위한 추상 클래스.
 * 데이터를 엑셀 시트에 추가하고 스타일을 정의하여 엑셀 문서를 만들 수 있다.
 *
 * @param <T> 엑셀 파일에 출력할 데이터 타입
 */
abstract class AbstractExcelView<T> : AbstractXlsView() {

    /**
     * HTTP 요청에서 데이터를 가져오는 추상 메서드.
     * 구현체에서 해당 메서드를 정의하여 데이터를 가져온다.
     *
     * @param request HTTP 요청 객체
     * @return 엑셀에 출력할 데이터 리스트
     */
    abstract fun getData(request: HttpServletRequest): List<T>

    /**
     * 엑셀 시트에 데이터를 채워 넣는 추상 메서드.
     * 구현체에서 해당 메서드를 정의하여 데이터를 시트에 삽입한다.
     *
     * @param sheet 엑셀 시트 객체
     * @param items 삽입할 데이터 리스트
     * @param bodyStyle 본문 스타일
     */
    abstract fun fillData(sheet: Sheet, items: List<T>, bodyStyle: CellStyle)

    /**
     * 엑셀 문서를 생성하는 메서드.
     * 엑셀 파일 제목, 헤더, 기간 등을 설정하고 데이터를 시트에 삽입한다.
     *
     * @param model 엑셀 문서에 필요한 데이터 모델
     * @param workbook 엑셀 워크북 객체
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @throws Exception 엑셀 문서 생성 중 발생하는 예외
     */
    @Throws(Exception::class)
    override fun buildExcelDocument(
        model: Map<String, Any>,
        workbook: Workbook,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val title = model["title"] as String? ?: "Excel Report"
        val headerTitles = model["headerTitles"] as List<String>
        val startDateStr = model["startDate"] as String
        val endDateStr = model["endDate"] as String
        val sheetName = model["sheetName"] as String? ?: "Sheet1"
        val fileName = (model["fileName"] as String?)?.let {
            if (it.endsWith(".xls")) it else "$it.xls"
        } ?: "${title}_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}.xls"

        val items = getData(request)

        response.setHeader("Content-Disposition", "attachment; filename=\"$fileName\"")

        val titleStyle = createTitleStyle(workbook)
        val headStyle = createHeadStyle(workbook)
        val bodyStyle = createBodyStyle(workbook)

        val sheet = workbook.createSheet(sheetName).apply {
            isDisplayGridlines = false
            autoSizeColumn(0)
        }

        createHeader(sheet, title, headerTitles, startDateStr, endDateStr, titleStyle, headStyle)
        fillData(sheet, items, bodyStyle)
    }

    /**
     * 엑셀 제목 스타일을 생성한다.
     *
     * @param workbook 엑셀 워크북 객체
     * @return 제목에 적용할 스타일 객체
     */
    private fun createTitleStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 16
            }
            setFont(font)
        }
    }

    /**
     * 엑셀 헤더 스타일을 생성한다.
     *
     * @param workbook 엑셀 워크북 객체
     * @return 헤더에 적용할 스타일 객체
     */
    private fun createHeadStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                color = IndexedColors.WHITE.index
            }
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            fillForegroundColor = HSSFColor.HSSFColorPredefined.GREY_80_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(font)
        }
    }

    /**
     * 엑셀 본문 스타일을 생성한다.
     *
     * @param workbook 엑셀 워크북 객체
     * @return 본문에 적용할 스타일 객체
     */
    private fun createBodyStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat("#,##0")
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    /**
     * 엑셀 시트에 제목과 헤더를 추가한다.
     *
     * @param sheet 엑셀 시트 객체
     * @param title 제목
     * @param headerTitles 헤더 타이틀 리스트
     * @param startDateStr 시작 날짜 문자열
     * @param endDateStr 종료 날짜 문자열
     * @param titleStyle 제목에 적용할 스타일
     * @param headStyle 헤더에 적용할 스타일
     */
    private fun createHeader(
        sheet: Sheet,
        title: String,
        headerTitles: List<String>,
        startDateStr: String,
        endDateStr: String,
        titleStyle: CellStyle,
        headStyle: CellStyle
    ) {
        val row0 = sheet.createRow(0).apply {
            createCell(0).apply {
                setCellValue(title)
                cellStyle = titleStyle
            }
        }

        val row1 = sheet.createRow(1).apply {
            val startDate = parseDate(startDateStr)
            val endDate = parseDate(endDateStr)
            createCell(0).setCellValue("조회기간 : ${startDate} ~ ${endDate}")
        }

        val row3 = sheet.createRow(3).apply {
            headerTitles.forEachIndexed { index, headerTitle ->
                createCell(index).apply {
                    setCellValue(headerTitle)
                    cellStyle = headStyle
                }
            }
        }
    }

    /**
     * 날짜 문자열을 분석하고 'yyyy-MM-dd' 형식으로 반환한다.
     * 지원되는 날짜 형식은 "yyyyMMdd", "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" 이다.
     *
     * @param dateStr 날짜 문자열
     * @return 파싱된 날짜 문자열 또는 'Invalid Date' 문자열
     */
    private fun parseDate(dateStr: String): String {
        val localDateFormats = listOf(
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )

        val localDateTimeFormats = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        )

        for (format in localDateFormats) {
            try {
                val date = LocalDate.parse(dateStr, format)
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: DateTimeParseException) {
                logger.warn("DateTimeParseException1 value: $e")
            }
        }

        for (format in localDateTimeFormats) {
            try {
                val dateTime = LocalDateTime.parse(dateStr, format)
                return dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: DateTimeParseException) {
                logger.warn("DateTimeParseException2 value: $e")
            }
        }

        logger.warn("Date parsing failed for value: $dateStr")
        return "Invalid Date"
    }

    /**
     * 엑셀 시트에 총합을 추가하는 메서드 (현재 주석 처리됨).
     *
     * @param sheet 엑셀 시트 객체
     * @param numberOfColumns 컬럼 수
     * @param bodyStyle 본문 스타일
     */
    private fun addTotalRow(sheet: Sheet, numberOfColumns: Int, bodyStyle: CellStyle) {
        val row = sheet.createRow(sheet.lastRowNum + 1)
        val cell = row.createCell(0).apply {
            setCellValue("Total")
            cellStyle = bodyStyle
        }
        for (i in 1 until numberOfColumns) {
            val cell = row.createCell(i)
            cell.cellStyle = bodyStyle
        }
    }
}
