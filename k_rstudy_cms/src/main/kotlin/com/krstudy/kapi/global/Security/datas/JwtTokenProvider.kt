package com.krstudy.kapi.com.krstudy.kapi.global.Security.datas
import io.jsonwebtoken.*
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import javax.crypto.SecretKey
import java.util.Date

@Component
class JwtTokenProvider(private val userDetailsService: UserDetailsService) {

    @Value("\${jwt.secret}")
    private lateinit var secretString: String

    private lateinit var secretKey: SecretKey

    @Value("\${jwt.expiration}")
    private var validityInMilliseconds: Long = 0

    @PostConstruct
    fun init() {
        // 보안 요구사항을 충족하는 키 생성
        secretKey = Jwts.SIG.HS256.key().build()
        // 또는 기존 시크릿 키를 사용하려면 아래와 같이 해시하여 사용
        // val hashedSecret = MessageDigest.getInstance("SHA-256")
        //     .digest(secretString.toByteArray())
        // secretKey = Keys.hmacShaKeyFor(hashedSecret)
    }

    fun createToken(username: String, roles: List<String>): String {
        val claims = mapOf<String, Any>(
            "sub" to username,
            "roles" to roles
        )

        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .claims(claims)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey, Jwts.SIG.HS256)  // 명시적으로 알고리즘 지정
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUsernameFromToken(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    fun getUserIdFromToken(token: String): String {
        val jws = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
        return jws.payload["userId", String::class.java]
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun getExpirationDateFromToken(token: String): Date {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .expiration
    }

    fun canTokenBeRefreshed(token: String, lastPasswordReset: Date): Boolean {
        val created = getExpirationDateFromToken(token)
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset) && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    private fun isCreatedBeforeLastPasswordReset(created: Date, lastPasswordReset: Date?): Boolean {
        return lastPasswordReset != null && created.before(lastPasswordReset)
    }
}
