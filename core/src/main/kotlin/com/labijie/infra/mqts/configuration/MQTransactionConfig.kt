package com.labijie.infra.mqts.configuration


open class MQTransactionConfig {
    var queues:HashMap<String, QueueConfig> = HashMap()
    var maxDataSizeBytes = 1048576

}