package com.krstudy.kapi.standard.base

import com.krstudy.kapi.domain.member.entity.Member
import com.lowagie.text.BadElementException
import com.lowagie.text.Cell
import com.lowagie.text.Document
import com.lowagie.text.Font
import com.lowagie.text.Image
import com.lowagie.text.Paragraph
import com.lowagie.text.Table
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfWriter
import org.springframework.core.io.ClassPathResource
import org.springframework.web.servlet.view.document.AbstractPdfView
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class MemberPdfView : AbstractPdfView() {

    private lateinit var fKorean: Font

    @Throws(Exception::class)
    override fun buildPdfDocument(
        model: Map<String, Any>,
        document: Document,
        writer: PdfWriter,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val calendar = GregorianCalendar()
        val fileName = "report_${sdf.format(calendar.time)}.pdf"
        response.setHeader("Content-Disposition", "attachment;filename=\"$fileName\"")

        val bfKorean = BaseFont.createFont("c:\\windows\\fonts\\batang.ttc,0",
            BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
        fKorean = Font(bfKorean)

        // 안전하게 Member 객체를 추출
        val member = model["member"] as? Member ?: throw IllegalArgumentException("Member not found in model")

        val table = Table(2)
        table.padding = 5f

        table.addCell(encoding("이름"))
        table.addCell(encoding(member.username ?: "이름 없음"))

        table.addCell(encoding("아이디"))
        table.addCell(encoding(member.userid))

        table.addCell(encoding("이메일"))
        table.addCell(encoding(member.userEmail))

        table.addCell(encoding("사진"))

        // accountType이 WEB이 아니면 profileImgUrl 사용, 그렇지 않으면 image 컬럼 값 사용
        val image: Image = if (member.accountType != "WEB") {
            if (member.profileImgUrl != null) {
                Image.getInstance(member.profileImgUrl)
            } else {
                val defaultImageUrl = "https://placehold.co/640x640?text=No+Image"
                Image.getInstance(defaultImageUrl)
            }
        } else {
            if (member.image != null) {
                Image.getInstance(member.image)
            } else {
                val defaultImageUrl = "https://placehold.co/640x640?text=No+Image"
                Image.getInstance(defaultImageUrl)
            }
        }

        // Image를 Cell에 직접 추가
        val imageCell = Cell(image)
        table.addCell(imageCell)

        document.add(table)
    }

    @Throws(BadElementException::class)
    private fun encoding(msg: String): Cell {
        return Cell(Paragraph(msg, fKorean))
    }
}