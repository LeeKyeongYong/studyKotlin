package com.krstudy.kapi.domain.messages.repository

import com.krstudy.kapi.domain.messages.entity.Message
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    // createDate는 BaseEntity에서 상속받은 필드이므로 이를 사용
    fun findByRecipientsRecipientIdOrderByCreateDateDesc(recipientId: Long, pageable: Pageable): Page<Message>

    // MessageRecipient의 readAt 필드를 참조하도록 수정
    @Query("""
        SELECT COUNT(DISTINCT m) 
        FROM Message m 
        JOIN MessageRecipient mr ON mr.message = m 
        WHERE mr.recipientId = :recipientId 
        AND mr.readAt IS NULL
    """)
    fun countUnreadMessagesByRecipientId(recipientId: Long): Long

    @Query("""
        SELECT DISTINCT m 
        FROM Message m 
        LEFT JOIN MessageRecipient mr ON mr.message = m 
        WHERE m.senderId = :userId 
        OR mr.recipientId = :userId
    """)
    fun findMessagesByUserId(userId: Long, pageable: Pageable): Page<Message>

    @Query("""
        SELECT DISTINCT m 
        FROM Message m 
        LEFT JOIN MessageRecipient mr ON mr.message = m 
        LEFT JOIN Member sender ON m.senderId = sender.id
        WHERE (m.senderId = :userId OR mr.recipientId = :userId)
        AND (LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(sender.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(mr.recipientName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
    """)
    fun searchMessages(userId: Long, searchTerm: String, pageable: Pageable): Page<Message>

    @Query("""
    SELECT DISTINCT m 
    FROM Message m 
    WHERE m.senderId = :userId
""")
    fun findBySenderId(userId: Long, pageable: Pageable): Page<Message>

    // 검색 기능을 위한 쿼리 추가
    @Query("""
    SELECT DISTINCT m 
    FROM Message m 
    WHERE m.senderId = :userId
    AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
""")
    fun searchSentMessages(userId: Long, searchTerm: String, pageable: Pageable): Page<Message>

    @Query("""
     SELECT DISTINCT m 
    FROM Message m 
    LEFT JOIN FETCH m.sender 
    LEFT JOIN MessageRecipient mr ON mr.message = m 
    WHERE mr.recipientUserId = :recipientUserId
""")
    fun findMessagesByRecipientUserId(recipientUserId: String, pageable: Pageable): Page<Message>

    @Modifying
    @Query("""
        UPDATE MessageRecipient mr 
        SET mr.readAt = CURRENT_TIMESTAMP 
        WHERE mr.message.id = :messageId AND mr.recipientId = :recipientId
    """)
    @Transactional  // 여기에도 @Transactional 추가
    fun markMessageAsRead(messageId: Long, recipientId: Long)

    @Query("""
        SELECT DISTINCT m 
        FROM Message m 
        JOIN MessageRecipient mr ON mr.message = m 
        WHERE mr.recipientId = :recipientId 
        AND mr.readAt IS NULL
    """)
    fun findUnreadMessagesByRecipientId(recipientId: Long): List<Message>
}