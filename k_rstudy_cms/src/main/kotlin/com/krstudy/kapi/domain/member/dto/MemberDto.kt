package com.krstudy.kapi.domain.member.dto

import com.krstudy.kapi.domain.member.entity.Member
import java.time.LocalDateTime

data class MemberDto(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val name: String,
    val userid: String,
    val profileImgUrl: String,
    val authorities: List<String>
) {
    companion object {
        fun from(member: Member): MemberDto {
            return MemberDto(
                id = member.id,
                userid = member.userid,
                createDate = member.getCreateDate() ?: LocalDateTime.now(), // 기본값 설정
                modifyDate = member.getModifyDate() ?: LocalDateTime.now(), // 기본값 설정
                name = member.username ?: "", // 기본값 설정
                profileImgUrl = member.getProfileImgUrlOrDefault(),
                authorities = member.getAuthoritiesAsStringList()
            )
        }
    }
}