package com.functionstudy.ch04.extensions

fun <T> T.discardUnless(predicate: (T) -> Boolean): T? {
    return if (predicate(this)) this else null
}