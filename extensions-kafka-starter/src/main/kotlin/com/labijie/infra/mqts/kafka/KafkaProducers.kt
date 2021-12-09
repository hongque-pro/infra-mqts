package com.labijie.infra.mqts.kafka

import com.labijie.infra.mqts.configuration.MQTransactionConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.LongSerializer
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-19
 */
class KafkaProducers(private val applicationName: String?, private val config: MQTransactionConfig) : AutoCloseable {
    companion object{
        private val logger by lazy { LoggerFactory.getLogger(KafkaProducers::class.java) }
    }
    private var producers: ConcurrentHashMap<String, KafkaProducer<Long, ByteArray>> = ConcurrentHashMap()

    private val keySerializer = LongSerializer()
    private val dataSerializer = ByteArraySerializer()

    override fun close() {
        this.producers.values.toTypedArray().forEach {
            closeProducer(it)
        }
        this.producers.clear()
    }

    private fun closeProducer(newProducer: KafkaProducer<Long, ByteArray>?) {
        try {
            newProducer!!.close()
        } catch (ex: KafkaException) {
            logger.warn("Close mqts kafka producer fault.", ex)
        }
    }

    private fun newProducer(queue: String): KafkaProducer<Long, ByteArray> {
        val conf = config.queues[queue]!!
        val properties = conf.loadKafkaProducerConfig()
        properties.putIfAbsent(ProducerConfig.ACKS_CONFIG, "0")
        properties.putIfAbsent(ProducerConfig.RETRIES_CONFIG, 1)
        if (!this.applicationName.isNullOrBlank()) {
            properties.putIfAbsent(ProducerConfig.CLIENT_ID_CONFIG, "mqts-$applicationName")
        }
        properties.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, 5)
        return KafkaProducer(properties, keySerializer, dataSerializer)
    }

    fun get(queue: String): KafkaProducer<Long, ByteArray> {
        if (queue.isBlank()) {
            throw IllegalArgumentException("MQTS queue cant not be null.")
        }
        if (!config.queues.containsKey(queue)) {
            throw IllegalArgumentException("MQTS is missing configuration for queue '$queue'.")
        }

        var newProducer: KafkaProducer<Long, ByteArray>? = null

        val p = this.producers.getOrPut(queue) {
            newProducer = newProducer(queue)
            newProducer
        }
        //就算键已经存在也可以会调用函数创建出新的 producer，通过引用比较销毁创建出的无用的 producer
        if (newProducer !== null && newProducer !== p) {
            closeProducer(newProducer)
        }
        return p
    }
}