package com.krstudy.kapi.global.Security.service

import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.global.Security.SecurityUser
import com.krstudy.kapi.global.Security.datas.Quadruple
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2Error


@Service
@Transactional(readOnly = true)
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    @Transactional
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        return runBlocking {
            try {
                val oAuth2User = super@CustomOAuth2UserService.loadUser(userRequest)
                val oauthId = oAuth2User.name
                val providerTypeCode = userRequest.clientRegistration.registrationId.uppercase()

                println("sns로그인 response: $oAuth2User")

                logger.debug("Processing OAuth2 user: $oauthId from provider: $providerTypeCode")

                val attributes = oAuth2User.attributes
                val (username, nickname, profileImgUrl, email) = extractUserInfo(providerTypeCode, attributes, oauthId)

                logger.info("Extracted user info - Username: $username, Nickname: $nickname")

                val modifiedEmail = when (providerTypeCode) {
                    "NAVER", "KAKAO","GOOGLE" -> email?.substringBefore('@') ?: "" // '@' 기호 없이 저장
                    else -> email // 기타 경우 이메일 그대로 사용
                }

                val result = memberService.modifyOrJoin(
                    username = username,
                    nickname = nickname,
                    providerTypeCode = providerTypeCode,
                    imageBytes = getDefaultImageBytes(),
                    profileImgUrl = profileImgUrl,
                    userid = modifiedEmail,
                    userEmail = email
                )

                val member = result.data ?: throw IllegalStateException("Failed to create or update member: ${result.msg ?: "Unknown error"}")

                logger.info("Successfully processed member: ${member.id}")

                SecurityUser(
                    id = member.id ?: throw IllegalStateException("Member ID cannot be null"),
                    _username = member.username ?: throw IllegalStateException("Username cannot be null"),
                    _password = member.password ?: "",
                    authorities = listOf(SimpleGrantedAuthority(member.roleType ?: "ROLE_MEMBER"))
                )
            } catch (e: Exception) {
                logger.error("Error in loadUser", e)
                throw OAuth2AuthenticationException(OAuth2Error("authentication_failed"), e.message ?: "Authentication failed", e)
            }
        }
    }

    private fun extractUserInfo(
        providerTypeCode: String,
        attributes: Map<String, Any>,
        oauthId: String
    ): Quadruple<String, String, String, String> { // 이메일을 추가해서 Quadruple로 반환
        return when (providerTypeCode) {
            "KAKAO" -> {
                println("KAKAO response: $attributes")
                val properties = attributes["properties"] as? Map<String, Any>
                    ?: throw IllegalStateException("Kakao properties not found")
                val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
                    ?: throw IllegalStateException("Kakao account not found")

                val userNames = kakaoAccount["name"] as? String ?: ""
                val email = kakaoAccount["email"] as? String ?: ""
                //사업자등록해서 처리가능  name_needs_agreement에서 가져오기 application-security: 사업자등록번호,키정리( phone_number 국제번호(+82 10-번호중간-번호 끝), gender성별, phone_number 전화번호,age_range 연령대,birthday 생일,birthyear 연도)

                Quadruple(
                    userNames,
                    properties["nickname"] as? String ?: "",
                    properties["profile_image"] as? String ?: "",
                    email // 이메일 추가
                )
            }
            "NAVER" -> {
                println("NAVER response: $attributes")
                val response = attributes["response"] as? Map<String, Any>
                    ?: throw IllegalStateException("Naver response not found")
                val email = response["email"] as? String ?: ""
                //age 나이, gender 성별, email 이메일, mobile 전화번호, mobile_e164 국제전화번호(+8210번호나머지8자리), name 이름, birthday 생일, birthyear 년도

                Quadruple(
                    response["name"] as? String ?: throw IllegalStateException("Naver name not found"),
                    response["nickname"] as? String ?: "",
                    response["profile_image"] as? String ?: "",
                    email // 이메일 추가
                )
            }
            "GOOGLE" -> {
                println("GOOGLE response: $attributes")

                val givenName = attributes["given_name"] as? String ?: throw IllegalStateException("Google given_name not found")
                val familyName = attributes["family_name"] as? String ?: ""
                // 이름을 합치기
                val NickName = "$familyName$givenName".trim() // trim()을 사용하여 불필요한 공백 제거


                Quadruple(
                    attributes["name"] as? String ?: throw IllegalStateException("Google name not found"),
                    NickName as? String ?: "",
                    attributes["picture"] as? String ?: "",
                    attributes["email"] as? String ?: "" // 이메일 추가
                )
            }
            else -> throw IllegalArgumentException("Unsupported provider: $providerTypeCode")
        }
    }

    private fun getDefaultImageBytes(): ByteArray {
        return try {
            val inputStream = this::class.java.classLoader.getResourceAsStream("gen/images/notphoto.jpg")
                ?: throw FileNotFoundException("Default image not found in resources")
            inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            logger.error("Error loading default image", e)
            ByteArray(0) // Return empty array as fallback
        }
    }

}