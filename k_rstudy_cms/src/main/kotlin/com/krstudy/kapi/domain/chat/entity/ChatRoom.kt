package com.krstudy.kapi.domain.chat.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatRoom(
    @Column(name = "room_name")
    val roomName: String? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    var author: Member? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false // 삭제 여부 필드 추가
) : BaseEntity()