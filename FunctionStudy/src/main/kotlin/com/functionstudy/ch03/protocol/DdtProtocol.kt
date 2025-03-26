package com.functionstudy.ch03.protocol

interface DdtProtocol<T> {

    fun addAction(action: T)
    fun executeAction(action: T)
    fun initializeActions(actions: List<T>)
    fun processActions(): Boolean
}