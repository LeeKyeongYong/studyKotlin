package com.krstudy.kapi.domain.trade.repository

import com.krstudy.kapi.domain.trade.entity.Coin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CoinRepository : JpaRepository<Coin, String> {
    @Query("SELECT c FROM Coin c ORDER BY c.code ASC")
    fun findAllByOrderByCodeAsc(): List<Coin>

    @Query("SELECT c FROM Coin c WHERE c.code = :code")
    fun findByCode(@Param("code") code: String): Coin?
}