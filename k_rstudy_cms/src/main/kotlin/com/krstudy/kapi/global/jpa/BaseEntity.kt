package com.krstudy.kapi.global.jpa

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import lombok.Getter
import lombok.ToString
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener::class)
@ToString(callSuper = true)
open class BaseEntity : IdEntity() {
    @Schema(hidden = true)
    @CreatedDate
    @Column(columnDefinition = "DATETIME(0)")
    private var createDate: LocalDateTime? = null

    @Schema(hidden = true)
    @LastModifiedDate
    @Column(columnDefinition = "DATETIME(0)")
    private var modifyDate: LocalDateTime? = null

    // Getters and Setters
    fun getCreateDate(): LocalDateTime? = createDate
    fun setCreateDate(createDate: LocalDateTime?) { this.createDate = createDate }

    fun getModifyDate(): LocalDateTime? = modifyDate
    fun setModifyDate(modifyDate: LocalDateTime?) { this.modifyDate = modifyDate }

    // Method to format LocalDateTime to a string in the desired format
    fun formatDateTime(dateTime: LocalDateTime?): String {
        return dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: ""
    }

    // Overriding toString to include formatted dates
    override fun toString(): String {
        return "BaseEntity(id=$id, createDate=${formatDateTime(createDate)}, modifyDate=${formatDateTime(modifyDate)})"
    }
}