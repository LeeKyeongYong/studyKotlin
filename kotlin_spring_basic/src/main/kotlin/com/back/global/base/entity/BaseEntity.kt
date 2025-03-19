package com.back.global.base.entity

import com.back.global.base.util.UtClass
import jakarta.persistence.*

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var _id: Long? = null

    val id: Long
        get() = _id ?: 0

    val modelName: String
        get() = UtClass.str.lcfirst(this::class.simpleName!!)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is BaseEntity) return false

        // 아래 비교는 modelName 이 아니라 Hibernate.getClass(other) 를 사용해도 됩니다.
        if (modelName != other.modelName) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return _id?.hashCode() ?: 0
    }
}