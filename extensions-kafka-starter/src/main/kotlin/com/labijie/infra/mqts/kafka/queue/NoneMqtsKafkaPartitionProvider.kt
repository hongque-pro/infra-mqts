package com.labijie.infra.mqts.kafka.queue

import com.labijie.infra.mqts.MQTransaction

class NoneMqtsKafkaPartitionProvider : IMqtsKafkaPartitionProvider {
    override fun getTransactionPartition(transaction: MQTransaction): Int? {
        return null
    }
}