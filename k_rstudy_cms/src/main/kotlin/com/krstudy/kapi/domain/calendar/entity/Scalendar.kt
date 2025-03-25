package com.krstudy.kapi.domain.calendar.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import java.util.ArrayList
@Entity
class Scalendar(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id") // JoinColumn 추가
    @Schema(description = "사용자 아이디 가져오기 위한 사용자 정보", example = "member_id")
    var author: Member? = null,

    @Schema(description = "달력 일정 제목", example = "TITLE")
    var title: String? = null,

    @Schema(description = "달력 일정 상세내용", example = "BODY")
    @Column(columnDefinition = "TEXT")
    var body: String? = null,

    @Schema(description = "달력 일정 상세내용 COUNT", example = "HIT")
    var hit: Long = 0,

    @Schema(description = "달력 일정 시작일자", example = "START_DAY")
    var startDay: String? = null,

    @Schema(description = "달력 일정 종료일자", example = "END_DAY")
    var endDay: String? = null,

    @Schema(description = "달력 일정 테두리", example = "FC_COLOR")
    @Column(name = "fColor") // DB 컬럼과 매핑
    var fcolor: String? = null // 색상코드
    //    @Schema(hidden = true) //날짜는 hidden
) : BaseEntity() {

    fun increaseHit() {
        hit++
    }

    fun getAuthorUsername(): String? {
        return author?.username
    }
}