package com.krstudy.kapi.standard.base

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * QR 코드를 생성하는 컴포넌트 클래스.
 * 주어진 URL을 QR 코드로 변환하여 바이트 배열로 반환하며, 파일 시스템에 QR 코드 이미지를 저장할 수 있다.
 */
@Component
class QRCodeGenerator {

    /**
     * QR 코드 생성에 사용될 인코딩 힌트를 설정한다.
     */
    private val hintMap: Map<EncodeHintType, Any> = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L
    )

    /**
     * 주어진 URL을 QR 코드로 변환하고, 지정된 너비와 높이로 이미지를 생성하여 바이트 배열로 반환한다.
     * 또한, 생성된 QR 코드를 파일 시스템에 저장한다.
     *
     * @param url QR 코드에 인코딩할 URL
     * @param width QR 코드 이미지의 너비
     * @param height QR 코드 이미지의 높이
     * @return 생성된 QR 코드 이미지의 바이트 배열
     * @throws IOException 파일 시스템에 QR 코드 이미지를 저장하는 도중 발생할 수 있는 예외
     */
    fun generateQRCode(url: String, width: Int, height: Int): ByteArray {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height, hintMap)

        ByteArrayOutputStream().use { outputStream ->
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
            val qrCodeImage = outputStream.toByteArray()

            // 파일 시스템에 저장
            FileUtils.saveQRCodeToFile(qrCodeImage, "D:\\intel2\\qr_code.png")
            return qrCodeImage
        }
    }

}
