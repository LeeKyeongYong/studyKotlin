package com.krstudy.kapi.domain.home.Controller;

import com.krstudy.kapi.domain.member.service.MemberService;
import com.krstudy.kapi.domain.post.entity.Post
import com.krstudy.kapi.global.exception.GlobalException;
import com.krstudy.kapi.global.https.ReqData;
import com.krstudy.kapi.domain.post.service.PostService
import com.krstudy.kapi.global.exception.MessageCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/b")
class BlogController(
    private val rq: ReqData,
    private val memberService: MemberService,
    private val postService: PostService
) {

    @GetMapping("/{username}")
    fun showList(
        @PathVariable username: String,
        @RequestParam(defaultValue = "") kw: String,
        @RequestParam(defaultValue = "1") page: Int
    ): String {
        val sorts = listOf(Sort.Order.desc("id"))
        val pageable: Pageable = PageRequest.of(page - 1, 10, Sort.by(sorts))

        val blogMember = memberService.findByUserid(username)
            ?: throw GlobalException(MessageCode.NOT_FOUND_USER)

        val postPage: Page<Post> = postService.search(blogMember, true, kw, pageable)

        rq.setAttribute("username", username)
        rq.setAttribute("postPage", postPage)
        rq.setAttribute("page", page)

        return "domain/post/blog/list"
    }

    @GetMapping("/{username}/{id}")
    fun showPostDetail(
        @PathVariable username: String,
        @PathVariable id: Long
    ): String {
        val blogMember = memberService.findByUserid(username)
            ?: throw GlobalException(MessageCode.NOT_FOUND_USER)
        val post = postService.findById(id)
            ?: throw GlobalException(MessageCode.NOT_FOUND_POST)

        rq.setAttribute("post", post)

        return "domain/post/post/detail"
    }
}