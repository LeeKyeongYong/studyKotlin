package com.krstudy.kapi.domain.banners.repository

import com.krstudy.kapi.domain.banners.entity.BannerEntity
import com.krstudy.kapi.domain.banners.enums.BannerStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BannerRepository : JpaRepository<BannerEntity, Long> {
    @Query("""
        SELECT b FROM Banner b 
        WHERE b.status = :status 
        AND b.startDate <= :now 
        AND b.endDate >= :now 
        ORDER BY b.displayOrder ASC, b.createDate DESC
    """)
    fun findActiveBanners(
        @Param("status") status: BannerStatus = BannerStatus.ACTIVE,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<BannerEntity>

    @Query("""
        SELECT b FROM Banner b 
        WHERE b.creator.userid = :userId 
        ORDER BY b.createDate DESC
    """)
    fun findByCreatorId(@Param("userId") userId: String): List<BannerEntity>

    @Query("""
        SELECT COUNT(b) FROM Banner b 
        WHERE b.status = :status 
        AND b.startDate <= :now 
        AND b.endDate >= :now
    """)
    fun countActiveBanners(
        @Param("status") status: BannerStatus = BannerStatus.ACTIVE,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Long
}