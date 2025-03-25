package com.krstudy.kapi.domain.item.repository

import com.krstudy.kapi.domain.item.entity.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, String>