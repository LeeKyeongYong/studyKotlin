package com.krstudy.kapi.domain.post.controller



import com.krstudy.kapi.domain.post.datas.ModifyForm
import com.krstudy.kapi.domain.post.datas.WriteForm
import com.krstudy.kapi.domain.post.entity.Post
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.domain.post.service.PostService
import com.krstudy.kapi.global.exception.MessageCode
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/post")
class PostController(
    private val postService: PostService,
    private val rq: ReqData
) {
    private val logger = LoggerFactory.getLogger(PostController::class.java)
    @GetMapping("/{id}")
    fun showDetail(@PathVariable id: Long): String {
        val post = postService.findById(id).orElseThrow { GlobalException(MessageCode.NOT_FOUND_POST) }
        postService.increaseHit(post)
        rq.setAttribute("post", post)
        return "domain/post/post/detail"
    }

    @GetMapping("/list")
    fun showList(
        @RequestParam(defaultValue = "") kw: String,
        @RequestParam(defaultValue = "1") page: Int
    ): String {
        val sorts = listOf(Sort.Order.desc("id"))
        val pageable: Pageable = PageRequest.of(page - 1, 10, Sort.by(sorts))
        val postPage: Page<Post> = postService.search(kw, pageable)
        rq.setAttribute("postPage", postPage)
        rq.setAttribute("page", page)
        return "domain/post/post/list"
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myList")
    fun showMyList(
        @RequestParam(defaultValue = "") kw: String,
        @RequestParam(defaultValue = "1") page: Int
    ): String {
        val sorts = listOf(Sort.Order.desc("id"))
        val pageable: Pageable = PageRequest.of(page - 1, 10, Sort.by(sorts))
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)

        val postPage: Page<Post> = postService.search(member, null, kw, pageable)

        // 수정된 println 호출
        println("postPage: $postPage")

        rq.setAttribute("postPage", postPage)
        rq.setAttribute("page", page)
        return "domain/post/post/myList"
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write")
    fun showWrite(): String = "domain/post/post/write"

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    fun write(@Valid @ModelAttribute form: WriteForm, redirectAttributes: RedirectAttributes): RedirectView {
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)
        val post = postService.write(member, form.title, form.body, form.isPublished)
        redirectAttributes.addFlashAttribute("message", "${post.id}번 글이 작성되었습니다.")
        return RedirectView("/post/${post.id}")
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/modify")
    fun showModify(@PathVariable id: Long, model: Model): String {
        val post = postService.findById(id).orElseThrow { GlobalException(MessageCode.NOT_FOUND_POST) }
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)
        if (!postService.canModify(member, post)) throw GlobalException(MessageCode.FORBIDDEN)
        model.addAttribute("post", post)
        return "domain/post/post/modify"
    }



    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/modify")
    fun modify(@PathVariable id: Long, @Valid @ModelAttribute form: ModifyForm, redirectAttributes: RedirectAttributes): RedirectView {
        val post = postService.findById(id).orElseThrow { GlobalException(MessageCode.NOT_FOUND_POST) }
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)
        if (!postService.canModify(member, post)) throw GlobalException(MessageCode.FORBIDDEN)
        postService.modify(post, form.title, form.body, form.isPublished)
        redirectAttributes.addFlashAttribute("message", "${post.id}번 글이 수정되었습니다.")
        return RedirectView("/post/${post.id}")
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/delete")
    fun delete(@PathVariable id: Long, redirectAttributes: RedirectAttributes): RedirectView {
        val post = postService.findById(id).orElseThrow { GlobalException(MessageCode.NOT_FOUND_POST) }
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)
        if (!postService.canDelete(member, post)) throw GlobalException(MessageCode.FORBIDDEN)
        postService.delete(post)
        redirectAttributes.addFlashAttribute("message", "${post.id}번 글이 삭제되었습니다.")
        return RedirectView("/post/list")
    }


    @PostMapping("/{id}/like")
    fun like(@PathVariable id: Long, redirectAttributes: RedirectAttributes): RedirectView {
        val post = postService.findById(id).orElseThrow { GlobalException(MessageCode.NOT_FOUND_POST) }
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)

        try {
            postService.like(member, post)
            redirectAttributes.addFlashAttribute("message", "${post.id}번 글을 추천하였습니다.")
        } catch (e: DataIntegrityViolationException) {
            // 중복 키 예외를 명시적으로 처리
            redirectAttributes.addFlashAttribute("message", "이미 추천한 글입니다.")
        } catch (e: GlobalException) {
            // 기존 예외 처리 로직
        }
        return RedirectView("/post/${post.id}")
    }


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/cancelLike")
    fun cancelLike(@PathVariable id: Long, redirectAttributes: RedirectAttributes): RedirectView {
        val post = postService.findById(id).orElseThrow { GlobalException(MessageCode.NOT_FOUND_POST) }
        val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)
        if (!postService.canCancelLike(member, post)) throw GlobalException(MessageCode.FORBIDDEN)
        postService.cancelLike(member, post)
        redirectAttributes.addFlashAttribute("message", "${post.id}번 글을 추천취소하였습니다.")
        return RedirectView("/post/${post.id}")
    }
}