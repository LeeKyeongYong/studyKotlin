package com.krstudy.kapi.test

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class JasyptConfigTest {

    private val encryptor: StandardPBEStringEncryptor = StandardPBEStringEncryptor().apply {
        setPassword("your-secret-password") // 암호화 및 복호화에 사용할 비밀번호 설정
        setAlgorithm("PBEWithMD5AndDES") // 사용할 암호화 알고리즘 설정
    }

    @Test
    fun jasypt() {
        val url = "jdbc:mysql://localhost:3307/msa"
        val username = "root"
        val password = "1234"

        val encryptUrl = jasyptEncrypt(url)
        val encryptUsername = jasyptEncrypt(username)
        val encryptPassword  = jasyptEncrypt(password)

        println("encryptUrl : $encryptUrl")
        println("encryptUsername : $encryptUsername")
        println("encryptPassword : $encryptPassword")

        assertEquals(url, jasyptDecrypt(encryptUrl))
    }

    private fun jasyptEncrypt(input: String): String {
        return encryptor.encrypt(input)
    }

    private fun jasyptDecrypt(input: String): String {
        return encryptor.decrypt(input)
    }
}
