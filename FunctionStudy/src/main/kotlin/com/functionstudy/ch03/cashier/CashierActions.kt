package com.functionstudy.ch03.cashier

import com.functionstudy.ch03.items.Item
import com.functionstudy.ch03.items.actions.DdtActions
import com.functionstudy.ch03.protocol.DdtProtocol

interface CashierActions : DdtProtocol<DdtActions> {
    fun setupPrices(prices: Map<String, Double>)
    fun totalForActorName(actorName: String): Double
    fun addItem(actorName: String, qty: Int, item: Item)
}