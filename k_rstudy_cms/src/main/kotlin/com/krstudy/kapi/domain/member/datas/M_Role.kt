package com.krstudy.kapi.domain.member.datas

enum class M_Role(val authority: String) {
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN"),
    HEADHUNTER("ROLE_HEADHUNTER"),
    MANAGER("ROLE_MANAGER"),
    HR("ROLE_HR");

    companion object {
        fun fromRoleType(roleType: String?): M_Role {
            return values().find { it.authority.equals(roleType, ignoreCase = true) } ?: MEMBER
        }

        fun getRoleType(role: M_Role): String {
            return role.authority
        }
    }
}
