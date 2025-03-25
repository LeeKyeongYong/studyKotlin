package com.krstudy.kapi.domain.passwd.service

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.passwd.entity.MemberSignature
import com.krstudy.kapi.domain.passwd.entity.PasswordChangeHistory
import com.krstudy.kapi.domain.passwd.repository.MemberSignatureRepository
import com.krstudy.kapi.domain.passwd.repository.PasswordChangeHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class PasswordChangeHistoryService(
    private val passwordChangeHistoryRepository: PasswordChangeHistoryRepository,
    private val memberSignatureRepository: MemberSignatureRepository
) {

    @Transactional
    fun savePasswordChangeHistory(
        member: Member,
        changeReason: String,
        signatureData: String?
    ): PasswordChangeHistory {
        // 서명 데이터가 있는 경우 저장
        if (!signatureData.isNullOrBlank()) {
            val memberSignature = MemberSignature(
                member = member,
                signatureData = signatureData
            )
            memberSignatureRepository.save(memberSignature)
        }

        val history = PasswordChangeHistory(
            member = member,
            changeReason = changeReason,
            signatureData = signatureData
        )

        return passwordChangeHistoryRepository.save(history)
    }

    fun getPasswordChangeHistory(memberId: Long): List<PasswordChangeHistory> {
        return passwordChangeHistoryRepository.findByMemberIdOrderByChangedAtDesc(memberId)
    }

    fun getMemberSignature(memberId: Long): MemberSignature? {
        return memberSignatureRepository.findByMemberId(memberId)
    }
}