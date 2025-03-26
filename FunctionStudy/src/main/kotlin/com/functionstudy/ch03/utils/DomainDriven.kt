package com.functionstudy.ch03.utils

import com.functionstudy.ch03.items.actions.DdtActions

abstract class DomainDriven<A : DdtActions>(private val actions: List<A>) {
    protected fun processActions() {
        actions.forEach { it.execute() }
    }
}