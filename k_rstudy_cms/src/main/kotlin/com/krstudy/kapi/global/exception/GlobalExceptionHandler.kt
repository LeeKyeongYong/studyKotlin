package com.krstudy.kapi.com.krstudy.kapi.global.exception

import com.krstudy.kapi.global.exception.CustomException
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.global.https.RespData
import com.krstudy.kapi.standard.base.Empty
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.NoHandlerFoundException
import java.io.PrintWriter
import java.io.StringWriter


@ControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler (private val rq: ReqData){

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException): ResponseEntity<Map<String, Any>> {
        val errorResponse = mapOf(
            "code" to ex.errorCode.code,
            "message" to ex.errorCode.message
        )
        return ResponseEntity(errorResponse, HttpStatus.valueOf(ex.errorCode.code.split("-")[0].toInt()))
    }
    // 자연스럽게 발생시킨 예외처리
    private fun handleApiException(ex: Exception): ResponseEntity<Any> {
        val body = LinkedHashMap<String, Any>().apply {
            put("resultCode", "500-1")
            put("statusCode", 500)
            put("msg", ex.localizedMessage)

            val data = LinkedHashMap<String, Any>()
            put("data", data)

            val sw = StringWriter()
            val pw = PrintWriter(sw)
            ex.printStackTrace(pw)
            data["trace"] = sw.toString().replace("\t", "    ").split(Regex("\\r\\n"))

            val path = rq.getCurrentUrlPath()
            data["path"] = path

            put("success", false)
            put("fail", true)
        }

        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    // 개발자가 명시적으로 발생시킨 예외처리
    @ExceptionHandler(GlobalException::class)
    fun handle(ex: GlobalException): ResponseEntity<RespData<Empty>> {
        val statusCode = try {
            ex.rsData.statusCode.takeIf { it > 0 } ?: 500
        } catch (e: Exception) {
            500
        }

        val status = HttpStatus.valueOf(statusCode)
        rq.setStatusCode(statusCode)

        return ResponseEntity(ex.rsData, status)
    }

    // 로깅 시 민감한 정보 마스킹
    private fun maskSensitiveInfo(info: String): String {
        return info.take(3) + "*".repeat(info.length - 3)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNotFound(): ModelAndView {
        return ModelAndView("redirect:/") // 루트 페이지로 리디렉션
    }

    // 추가: HTML 요청에 대한 예외 처리
    @ExceptionHandler(Exception::class)
    fun handleHtmlException(ex: Exception): String {
        return "redirect:/"  // 에러 페이지 템플릿 경로
    }

}