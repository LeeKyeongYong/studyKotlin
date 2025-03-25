package com.krstudy.kapi.domain.calendar.controller

import com.krstudy.kapi.domain.calendar.entity.Scalendar
import com.krstudy.kapi.domain.calendar.service.ScalendarService
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.member.service.MemberService
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*

/**
 * `RestCalendarController`는 달력과 관련된 CRUD 작업을 처리하는 RESTful API를 제공합니다.
 * 이 컨트롤러는 스케줄(일정)과 관련된 다양한 엔드포인트를 제공하며,
 * 인증된 사용자가 자신의 스케줄을 관리할 수 있도록 합니다.
 */
@RestController
@RequestMapping("v1/scalendar")
@OpenAPIDefinition(info = Info(title = "달력 리스트 처리요청 API", description = "달력 리스트 처리요청 API", version = "v1"))
class RestCalendarController @Autowired constructor(
    private val scalendarService: ScalendarService,
    private val memberService: MemberService // UserService 의존성 주입
) {

    /**
     * 모든 일정을 조회합니다.
     *
     * @return `200 OK`와 함께 일정 목록을 반환합니다.
     * @response `501` 오류 발생 시 API 예외 응답을 반환합니다.
     */
    @Operation(summary = "일정리스트 요청", description = "일정 리스트요청 진행할 수 있다.", tags = ["getCalendarAllList"])
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "SUCCESS"),
        ApiResponse(responseCode = "501", description = "API EXCEPTION")
    ])
    @GetMapping
    fun getAllScalendarEvents(): ResponseEntity<List<Map<String, Any?>>> {
        val events = scalendarService.getAllScalendarEvents()
        val response = events.map { event ->
            mapOf(
                "id" to event.id,
                "title" to event.title,
                "start" to event.startDay,
                "end" to event.endDay,
                "backgroundColor" to event.fcolor
            )
        }
        return ResponseEntity(response, HttpStatus.OK)
    }

    /**
     * 새로운 일정을 생성합니다.
     *
     * @param scalendar 생성할 일정의 정보
     * @return 생성된 일정과 함께 `201 Created` 응답을 반환합니다.
     * @response `404` 사용자를 찾을 수 없는 경우의 오류 응답
     * @response `500` 서버 오류 발생 시의 오류 응답
     */
    @PostMapping
    fun createScalendar(@RequestBody scalendar: Scalendar): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            val username = (authentication.principal as UserDetails).username
            val author: Member = memberService.findByUserid(username)
                ?: throw UsernameNotFoundException("User not found with username: $username")
            scalendar.author = author
            val createdScalendar = scalendarService.createScalendar(scalendar)
            ResponseEntity(createdScalendar, HttpStatus.CREATED)
        } catch (e: UsernameNotFoundException) {
            e.printStackTrace()
            ResponseEntity(mapOf("error" to "User not found: ${e.message}"), HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity(mapOf("error" to "An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * 특정 ID를 가진 일정을 조회합니다.
     *
     * @param id 조회할 일정의 ID
     * @return `200 OK`와 함께 일정 정보를 반환합니다.
     * @response `404` 일정이 존재하지 않는 경우의 오류 응답
     */
    @GetMapping("/{id}")
    fun getScalendarById(@PathVariable id: Long): ResponseEntity<Scalendar> {
        val scalendar = scalendarService.getScalendarById(id)
        return if (scalendar != null) {
            ResponseEntity(scalendar, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    /**
     * 특정 ID를 가진 일정을 업데이트합니다.
     *
     * @param id 업데이트할 일정의 ID
     * @param updatedScalendar 업데이트할 일정의 정보
     * @return 업데이트된 일정과 함께 `200 OK` 응답을 반환합니다.
     * @response `404` 일정이 존재하지 않는 경우의 오류 응답
     */
    @PutMapping("/{id}")
    fun updateScalendar(
        @PathVariable id: Long,
        @RequestBody updatedScalendar: Scalendar
    ): ResponseEntity<Scalendar> {
        val scalendar = scalendarService.updateScalendar(id, updatedScalendar)
        return if (scalendar != null) {
            ResponseEntity(scalendar, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    /**
     * 특정 ID를 가진 일정을 삭제합니다.
     *
     * @param id 삭제할 일정의 ID
     * @return `204 No Content` 응답을 반환합니다.
     */
    @DeleteMapping("/{id}")
    fun deleteScalendar(@PathVariable id: Long): ResponseEntity<Void> {
        scalendarService.deleteScalendar(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * 특정 ID를 가진 일정의 조회수를 증가시킵니다.
     *
     * @param id 조회수를 증가시킬 일정의 ID
     * @return `204 No Content` 응답을 반환합니다.
     */
    @PatchMapping("/{id}/hit")
    fun increaseHit(@PathVariable id: Long): ResponseEntity<Void> {
        scalendarService.increaseHit(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}