package com.labijie.infra.mqts.kafka.queue

import com.labijie.infra.mqts.MQTransaction

interface IMqtsKafkaPartitionProvider {
    fun getTransactionPartition(transaction:MQTransaction): Int?
}