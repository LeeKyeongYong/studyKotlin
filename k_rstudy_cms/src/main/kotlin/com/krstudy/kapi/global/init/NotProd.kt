package com.krstudy.kapi.global.init

import com.krstudy.kapi.domain.member.datas.M_Role
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.domain.post.entity.Post
import com.krstudy.kapi.domain.post.service.PostService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.transaction.annotation.Transactional
import java.io.FileNotFoundException

data class MemberInfo(val userid: String, val username: String, val authority: String? = null)



@Configuration
@Profile("prod")
open class NotProd(
    private val memberService: MemberService,
    private val postService: PostService
) {
    private val log = LoggerFactory.getLogger(NotProd::class.java)

    @Bean
    @Order(3)
    fun initNotProd(): ApplicationRunner {
        return ApplicationRunner {
            runBlocking {
                if (memberService.findByUserid("user1") == null) {
                    initializeMembersAndPosts()
                }
            }
        }
    }

    @Transactional
    open suspend fun initializeMembersAndPosts() {
        val defaultImageBytes = getDefaultImageBytes() // 기본 이미지 바이트 가져오기

        val members = listOf(
            MemberInfo("user1", "일반 사용자1", M_Role.MEMBER.authority),
            MemberInfo("user2", "일반 사용자2", M_Role.MEMBER.authority),
            MemberInfo("user3", "일반 사용자3", M_Role.MEMBER.authority),
            MemberInfo("user4", "일반 사용자4", M_Role.MEMBER.authority),
            MemberInfo("m_user01", "그룹 운영자", M_Role.HR.authority),
            MemberInfo("h_user01", "인사 담당자", M_Role.HEADHUNTER.authority),
            MemberInfo("d_user5", "임시 관리자", M_Role.ADMIN.authority),
            MemberInfo("h_user02", "헤드 헌터", M_Role.HEADHUNTER.authority),
            MemberInfo("d_user2", "인사 담당자", M_Role.HR.authority)
        )

        val createdMembers = members.mapNotNull { memberInfo ->
            log.info("Joining member with authority: ${memberInfo.authority}") // 로깅 추가
            memberService.join(
                memberInfo.userid,
                memberInfo.username,
                memberInfo.username,
                "abc@lky.co.kr",
                "1234",
                "image/jpeg",
                defaultImageBytes, // 기본 이미지 바이트 전달
                memberInfo.authority,// 각 사용자에 맞는 권한 전달
                "",
                ""
            )?.data
        }



        // 게시물 작성 로직은 그대로 유지
        val memberUser1 = createdMembers[0]
        val postIds = (1..4).map { i -> postService.write(memberUser1, "제목 $i", "내용 $i", true) }

        createdMembers.forEachIndexed { index, member ->
            if (index > 0) {
                postService.write(member, "제목 ${index + 4}", "내용 ${index + 4}", true)
            }
        }

        // 댓글 작성 및 좋아요
        postIds.forEach { post ->
            createdMembers.forEach { member ->
                postService.writeComment(memberUser1, post, "안녕하세요! 댓글입니다.")
            }
        }

        likePosts(createdMembers, postIds)
    }

    private fun getDefaultImageBytes(): ByteArray {
        val inputStream = this::class.java.classLoader.getResourceAsStream("gen/images/notphoto.jpg")
            ?: throw FileNotFoundException("Default image not found in resources")

        return inputStream.readBytes().also {
            inputStream.close() // InputStream을 닫기
        }
    }

    private fun likePosts(members: List<Member>, posts: List<Post>) {
        val likePairs = listOf(
            Pair(members[1], posts[0]),
            Pair(members[2], posts[0]),
            Pair(members[3], posts[0]),
            Pair(members[1], posts[1]),
            Pair(members[2], posts[1]),
            Pair(members[1], posts[2])
        )

        likePairs.forEach { (member, post) ->
            if (postService.canLike(member, post)) {
                postService.like(member, post)
            } else {
                log.info("이미 좋아요를 누른 사용자: ${member.userid}, 게시물: ${post.id}")
            }
        }
    }
}
