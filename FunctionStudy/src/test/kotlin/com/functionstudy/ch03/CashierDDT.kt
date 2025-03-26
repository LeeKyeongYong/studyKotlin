package com.functionstudy.ch03

import com.functionstudy.ch03.cashier.Cashier
import com.functionstudy.ch03.items.Item
import com.functionstudy.ch03.items.actions.DdtActions
import org.junit.jupiter.api.Test
import com.functionstudy.ch03.utils.*

class CashierDDT : DomainDriven<DdtActions>(allActions) {
    @DDT
    fun `고객은 아이템을 구매할 수 있다`() = ddtScenario {
        val prices = mapOf(
            Item.CARROT.name to 2.0,
            Item.MILK.name to 5.0
        )
        val cashier = Cashier()

        give {
            cashier.setupPrices(prices)
        }

        whenPlay {
            val alice = NamedActor("Alice")
            alice.`아이템을 #qty 추가한다`(3, Item.CARROT)
            alice.`아이템을 #qty 추가한다`(1, Item.MILK)
        }

        then {
            val alice = NamedActor("Alice")
            alice.`총합이 #total인지 확인`(11.0)
        }
    }

    @Test
    fun `문자열 위치 반환하기`() {
        val myCharAtPos = buildCharAtPos("Kotlin")
        println("첫 번째 문자: ${myCharAtPos(0)}")
        expectThat(myCharAtPos(0)).isEqualTo('K')
    }

    @Test
    fun `템플릿 엔진 테스트`() {
        val template = "Happy Birthday {name} {surname}! from {sender}."
        val data = mapOf(
            "name" to StringTag("Uberto"),
            "surname" to StringTag("Barbini"),
            "sender" to StringTag("PragProg")
        )
        val actual = renderTemplate(template, data)
        val expected = "Happy Birthday Uberto Barbini! from PragProg."
        println("실제 출력: $actual")
        println("예상 출력: $expected")
        expectThat(actual).isEqualTo(expected)
    }
}
