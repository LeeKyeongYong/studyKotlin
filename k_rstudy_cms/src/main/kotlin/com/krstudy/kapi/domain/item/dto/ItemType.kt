package com.krstudy.kapi.domain.item.dto

enum class ItemType(val cd: String, val desc: String) {
    FOOD("F", "음식"),
    CLOTHES("C", "옷");

    fun hasItemCd(cd: String): Boolean {
        return this.cd == cd
    }
}
