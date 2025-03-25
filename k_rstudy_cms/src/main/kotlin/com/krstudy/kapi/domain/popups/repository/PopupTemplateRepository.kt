package com.krstudy.kapi.domain.popups.repository

import com.krstudy.kapi.domain.popups.entity.PopupTemplateEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PopupTemplateRepository : JpaRepository<PopupTemplateEntity, Long> {
    @Query("SELECT p FROM PopupTemplate p WHERE p.isDefault = true")
    fun findByIsDefaultTrue(): List<PopupTemplateEntity>

    @Query("SELECT p FROM PopupTemplate p WHERE p.isDefault = false")
    fun findByIsDefaultFalse(): List<PopupTemplateEntity>

    fun findByCreatorUserid(userid: String): List<PopupTemplateEntity>
}