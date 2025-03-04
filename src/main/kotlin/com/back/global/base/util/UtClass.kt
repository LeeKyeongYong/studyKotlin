package com.back.global.base.util

import java.util.*

object UtClass {
    object str {
        fun lcfirst(str: String): String {
            return str.substring(0, 1).lowercase(Locale.getDefault()) + str.substring(1)
        }
    }
}