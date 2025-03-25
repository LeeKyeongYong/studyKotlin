package com.krstudy.kapi.domain.uploads.repository

import com.krstudy.kapi.domain.uploads.dto.FileStatusEnum
import com.krstudy.kapi.domain.uploads.entity.FileEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UploadFileRepository : JpaRepository<FileEntity, Long> {
    // StoredFile 엔티티 이름을 사용하는 쿼리
    @Query("SELECT f FROM StoredFile f WHERE f.member.userid = :userId AND f.status = :status")
    fun findAllByUserId(
        @Param("userId") userId: String,
        @Param("status") status: FileStatusEnum = FileStatusEnum.ACTIVE
    ): List<FileEntity>

    // 체크섬으로 중복 확인
    @Query("SELECT COUNT(f) > 0 FROM StoredFile f WHERE f.checksum = :checksum AND f.status = 'ACTIVE'")
    fun existsByChecksumAndStatus(@Param("checksum") checksum: String): Boolean

    // 파일 이름으로 검색
    @Query("SELECT f FROM StoredFile f WHERE f.storedFileName = :storedFileName AND f.status = :status")
    fun findByStoredFileNameAndStatus(
        @Param("storedFileName") storedFileName: String,
        @Param("status") status: FileStatusEnum
    ): FileEntity?

    // 활성화된 파일 검색
    @Query("SELECT f FROM StoredFile f WHERE f.status = 'ACTIVE' AND f.id = :id")
    fun findActiveFileById(@Param("id") id: Long): FileEntity?

    // PDF 파일 목록 조회 (페이징)
    @Query("SELECT f FROM StoredFile f WHERE f.status = :status AND f.fileType = :fileType")
    fun findAllByStatusAndFileType(
        @Param("status") status: FileStatusEnum,
        @Param("fileType") fileType: String,
        pageable: Pageable
    ): Page<FileEntity>
}