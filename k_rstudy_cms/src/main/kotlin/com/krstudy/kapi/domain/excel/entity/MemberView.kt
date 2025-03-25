package com.krstudy.kapi.domain.excel.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
@Entity
@Immutable
@Table(name = "member_view")
class MemberView(

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성 전략을 추가할 수 있습니다.
    val memberId: Long? = null,

    @Column(name = "userid")
    val userid: String? = null,

    @Column(name = "username")
    val username: String? = null,

    @Column(name = "role_type")
    val roleType: String? = null,

    @Column(name = "password")
    val password: String? = null,

    @Column(name = "authorities")
    val authorities: String? = null,

    @Column(name = "is_admin")
    val isAdmin: Boolean? = null
)