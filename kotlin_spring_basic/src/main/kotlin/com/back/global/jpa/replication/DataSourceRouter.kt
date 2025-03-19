package com.back.global.jpa.replication

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.*


class DataSourceRouter : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            val random = Random()
            val randomValue = random.nextInt(2) + 1
            return randomValue
        }

        return 0
    }
}