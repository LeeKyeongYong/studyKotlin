package com.krstudy.kapi.standard.base

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 파일 유틸리티 클래스.
 * QR 코드 이미지 파일을 저장하고, 날짜 기반 파일 경로를 생성하며, 디렉토리 존재 여부를 확인하는 기능을 제공한다.
 */
object FileUtils {

    /**
     * 주어진 QR 코드 이미지를 파일로 저장한다.
     * 파일 저장 경로는 날짜와 시간을 포함한 이름으로 생성된다.
     *
     * @param image 저장할 QR 코드 이미지 바이트 배열
     * @param baseFilePath 파일을 저장할 기본 경로
     */
    fun saveQRCodeToFile(image: ByteArray, baseFilePath: String) {
        val filePath = generateFilePathWithDate(baseFilePath)
        val path = Paths.get(filePath)
        ensureDirectoryExists(path)
        try {
            Files.write(path, image)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 날짜와 시간을 포함한 파일 경로를 생성한다.
     * 파일명은 "qr_code_yyyyMMddHHmmss.png" 형식으로 생성된다.
     *
     * @param baseFilePath 기본 파일 경로
     * @return 날짜와 시간을 포함한 파일 경로 문자열
     */
    fun generateFilePathWithDate(baseFilePath: String): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val now = LocalDateTime.now().format(dateFormatter)
        val fileName = "qr_code_$now.png"
        return Paths.get(baseFilePath).parent.resolve(fileName).toString()
    }

    /**
     * 주어진 경로에 디렉토리가 존재하는지 확인하고, 존재하지 않으면 디렉토리를 생성한다.
     *
     * @param path 파일 경로
     */
    fun ensureDirectoryExists(path: Path) {
        val parentPath: Path = path.parent
        if (!Files.exists(parentPath)) {
            try {
                Files.createDirectories(parentPath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}