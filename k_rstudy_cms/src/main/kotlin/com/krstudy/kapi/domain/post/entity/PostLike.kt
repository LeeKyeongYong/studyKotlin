package com.krstudy.kapi.domain.post.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import lombok.NoArgsConstructor
import org.hibernate.annotations.NaturalId

@Entity
@Table(name = "post_like")
//@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["post_id", "member_id"])])
class PostLike(
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: Post,

    @ManyToOne
    @JoinColumn(name = "member_id")
    var member: Member
) : BaseEntity(){}
