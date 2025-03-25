package com.krstudy.kapi.domain.uploads.service

import FileService
import com.krstudy.kapi.domain.member.repository.MemberRepository
import com.krstudy.kapi.domain.uploads.dto.FileStatusEnum
import com.krstudy.kapi.domain.uploads.entity.FileEntity
import com.krstudy.kapi.domain.uploads.exception.FileUploadException
import com.krstudy.kapi.domain.uploads.repository.UploadFileRepository
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

// FileServiceImpl.kt
@Service("uploadFileService")
class FileServiceImpl(
    private val fileRepository: UploadFileRepository,
    private val memberRepository: MemberRepository,  // Member 레포지토리 추가
    @Value("\${file.upload-dir.windows}") private val uploadDir: String
) : FileService {
    private val logger = LoggerFactory.getLogger(FileServiceImpl::class.java)

    @PostConstruct
    fun init() {
        createUploadDirectory()
    }

    @Transactional
    override fun uploadFiles(files: Array<MultipartFile>, userId: String): List<FileEntity> {
        // userId로 변경
        val member = memberRepository.findByUserid(userId)
            ?: throw EntityNotFoundException("Member not found with userId: $userId").also {
                logger.error("Member with userId $userId not found.")
            }

        logger.info("Uploading files for member: $member")

        return files.mapNotNull { file ->
            try {
                if (file.isEmpty) {
                    logger.warn("Skipping empty file: ${file.originalFilename}")
                    return@mapNotNull null
                }

                val checksum = calculateChecksum(file)
                if (fileRepository.existsByChecksumAndStatus(checksum)) {
                    logger.warn("File with same checksum already exists: ${file.originalFilename}")
                    return@mapNotNull null
                }

                // 원본 파일명에서 확장자 추출
                val originalFilename = file.originalFilename ?: "unknown"
                val originalExt = originalFilename.substringAfterLast(".", "")

                // UUID + 원본 확장자로 저장 파일명 생성
                val storedFileName = if (originalExt.isNotEmpty()) {
                    "${UUID.randomUUID()}.${originalExt}"
                } else {
                    UUID.randomUUID().toString()
                }

                // 전체 파일 경로 생성
                val filePath = Path.of(uploadDir).resolve(storedFileName)

                // 디렉토리가 존재하는지 확인하고 생성
                Files.createDirectories(filePath.parent)

                // 파일 복사 수행
                Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

                val fileEntity = FileEntity(
                    originalFileName = originalFilename,
                    storedFileName = storedFileName,
                    filePath = filePath.toString(),
                    fileSize = file.size,
                    fileType = file.contentType ?: "application/octet-stream",
                    contentType = file.contentType ?: "application/octet-stream",
                    checksum = checksum,
                    member = member  // Member 엔티티 설정
                )

                // 파일 엔티티 저장
                return@mapNotNull fileRepository.save(fileEntity).also {
                    logger.info("File saved successfully: ${it.id} - ${it.originalFileName}")
                }

            } catch (e: Exception) {
                logger.error("Failed to upload file: ${file.originalFilename}, Error: ${e.message}", e)
                throw FileUploadException("Failed to upload file: ${file.originalFilename}")
            }
        }
    }

    override fun getUserFiles(userId: String): List<FileEntity> {
        return fileRepository.findAllByUserId(userId)
    }

    @Transactional
    override fun deleteFile(fileId: Long, userId: String) {
        val file = getFileById(fileId)
        if (file.member?.userid != userId) {
            throw SecurityException("Not authorized to delete this file")
        }
        file.status = FileStatusEnum.DELETED  // FileEntity.FileStatus 대신 FileStatusEnum 사용
        fileRepository.save(file)
    }

    private fun createUploadDirectory() {
        try {
            val directory = Path.of(uploadDir)
            if (!Files.exists(directory)) {
                Files.createDirectories(directory)
                logger.info("Successfully created directory: $uploadDir")
            }

            if (!Files.isWritable(directory)) {
                logger.error("No write permission for directory: $uploadDir")
                throw RuntimeException("No write permission for upload directory")
            }

            logger.info("Upload directory ready at: $uploadDir")
        } catch (e: Exception) {
            logger.error("Error creating upload directory: ${e.message}", e)
            throw RuntimeException("Could not create upload directory", e)
        }
    }

    private fun calculateChecksum(file: MultipartFile): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(file.bytes)
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            logger.error("Failed to calculate checksum", e)
            throw FileUploadException("Failed to calculate checksum: ${e.message}")
        }
    }

    fun getAllActivePdfFiles(page: Int, size: Int = 10): Page<FileEntity> {
        val pageable = PageRequest.of(page, size, Sort.by("createDate").descending())
        return fileRepository.findAllByStatusAndFileType(
            FileStatusEnum.ACTIVE,
            "application/pdf",
            pageable
        )
    }

    override fun getFileById(id: Long): FileEntity {
        return fileRepository.findById(id)
            .orElseThrow { EntityNotFoundException("File not found with id: $id") }
    }
}