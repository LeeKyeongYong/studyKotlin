package com.krstudy.kapi.domain.files.service

import com.krstudy.kapi.domain.files.entity.FileEntity
import com.krstudy.kapi.domain.files.repository.FileRepository
import com.krstudy.kapi.global.exception.FileSaveException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileService(
    private val fileRepository: FileRepository,
    @Value("\${file.upload-dir.windows:#{null}}") private val windowsUploadDir: String?,
    @Value("\${file.upload-dir.linux:#{null}}") private val linuxUploadDir: String?
) {
    private val logger = LoggerFactory.getLogger(FileService::class.java)

    init {
        logger.info("Windows upload dir: $windowsUploadDir")
        logger.info("Linux upload dir: $linuxUploadDir")
    }

    private val uploadDir: String by lazy {
        when {
            !windowsUploadDir.isNullOrBlank() -> windowsUploadDir
            !linuxUploadDir.isNullOrBlank() -> linuxUploadDir
            else -> System.getProperty("java.io.tmpdir")
        }.also { dir ->
            logger.info("Selected upload directory: $dir")
            val directory = File(dir)
            if (!directory.exists() && !directory.mkdirs()) {
                logger.error("Failed to create directory: ${directory.absolutePath}")
                throw IllegalStateException("Failed to create directory: ${directory.absolutePath}")
            }
            if (!directory.canWrite()) {
                logger.error("No write permission for directory: ${directory.absolutePath}")
                throw IllegalStateException("No write permission for directory: ${directory.absolutePath}")
            }
            logger.info("Upload directory initialized: $dir")
        }
    }

    fun saveFile(file: MultipartFile, relatedEntityId: Long, entityType: String): FileEntity {
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Filename cannot be null")
        val storedFilename = "${UUID.randomUUID()}_$originalFilename"
        val path: Path = Paths.get(uploadDir, storedFilename)

        logger.info("Attempting to save file: $originalFilename")
        logger.info("Full path: ${path.toAbsolutePath()}")

        try {
            Files.copy(file.inputStream, path, StandardCopyOption.REPLACE_EXISTING)
            logger.info("File saved successfully: $storedFilename")
        } catch (e: IOException) {
            logger.error("Failed to save file: ${e.message}", e)
            throw FileSaveException("Failed to save file: ${e.message}", e)
        }

        val fileEntity = FileEntity(
            originalFilename = originalFilename,
            storedFilename = storedFilename,
            relatedEntityId = relatedEntityId,
            entityType = entityType
        )
        return fileRepository.save(fileEntity).also {
            logger.info("File entity saved: ${it.id}")
        }
    }
}