package com.labijie.infra.mqts.kafka

import com.labijie.infra.mqts.configuration.QueueConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

private fun QueueConfig.loadConfig(prefix: String, configKeys: Set<String>): Properties {
    val properties = Properties()
    this.properties.forEach {
        val v = it.value.toString()
        if(v.isNotBlank()) {
            if (it.key in configKeys) {
                properties.setProperty(it.key, v)
            } else if (it.key.startsWith(prefix)) {
                val k = it.key.removePrefix(prefix)
                if (k in configKeys) {
                    properties.setProperty(k, v)
                }
            }
        }
    }
    if (this.server.isNotBlank()) {
        properties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = this.server
    }
   return properties
}

fun QueueConfig.loadKafkaConsumerConfig() = this.loadConfig("consumer.", ConsumerConfig.configNames())

fun QueueConfig.loadKafkaProducerConfig() = this.loadConfig("producer.", ProducerConfig.configNames())