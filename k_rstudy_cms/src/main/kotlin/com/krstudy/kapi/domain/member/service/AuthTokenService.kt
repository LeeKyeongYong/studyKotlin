package com.krstudy.kapi.domain.member.service

import com.krstudy.kapi.domain.member.entity.Member
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKey
import java.util.Base64

@Service
class AuthTokenService (
    @Value("\${custom.jwt.secretKey}")
    private val jwtSecretKey: String,

    @Value("\${custom.accessToken.expirationSec}")
    private val accessTokenExpirationSec: Long
) {
    // SecretKey 객체를 생성합니다.
    private val secretKey: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    fun genToken(member: Member, expireSeconds: Long): String {
        val claims = Jwts.claims()
            .add("id", member.id)
            .add("username", member.username)
            .add("authorities", member.getAuthoritiesAsStringList())
            .build()
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + 1000 * expireSeconds)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun genAccessToken(member: Member): String {
        return genToken(member, accessTokenExpirationSec)
    }

    fun getDataFrom(token: String): Map<String, Any?> {
        val payload = Jwts.parser()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body

        return mapOf(
            "id" to payload.get("id", Integer::class.java),
            "username" to payload.get("username", String::class.java),
            "authorities" to payload.get("authorities", List::class.java)
        )
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun genRefreshToken(userId: String): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}