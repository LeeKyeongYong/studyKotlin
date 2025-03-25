package com.krstudy.kapi.domain.item.entity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import lombok.*

@Entity
@Table(name = "ITEM")
@NoArgsConstructor
@AllArgsConstructor
data class Item(
    @Id
    @Column(name = "ID", length = 30)
    var id: String,

    @Column(name = "ACCOUNT_ID")
    var accountId: String? = null,

    @Column(name = "NAME", length = 30)
    var name: String? = null,

    @Column(name = "DESCRIPTION", length = 30)
    var description: String? = null,

    @Column(name = "ITEM_TYPE", length = 1)
    var itemType: String? = null,

    @Column(name = "CNT", length = 10)
    var count: Long,

    @Column(name = "REG_DTS", length = 14)
    var regDts: String? = null,

    @Column(name = "UPD_DTS", length = 14)
    var updDts: String? = null
)
