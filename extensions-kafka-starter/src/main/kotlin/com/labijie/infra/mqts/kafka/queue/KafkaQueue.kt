package com.labijie.infra.mqts.kafka.queue

import com.labijie.infra.mqts.MQTransaction
import com.labijie.infra.mqts.MQTransactionException
import com.labijie.infra.mqts.abstractions.ITransactionHolder
import com.labijie.infra.mqts.abstractions.ITransactionQueue
import com.labijie.infra.mqts.configuration.MQTransactionConfig
import com.labijie.infra.mqts.kafka.Constants
import com.labijie.infra.mqts.kafka.IKafkaRecordHandler
import com.labijie.infra.mqts.kafka.KafkaConsumers
import com.labijie.infra.mqts.kafka.KafkaProducers
import com.labijie.infra.mqts.scopeWith
import com.labijie.infra.utils.throwIfNecessary
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.record.TimestampType
import org.apache.kafka.common.serialization.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import java.util.concurrent.ConcurrentHashMap

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-17
 */
class KafkaQueue(
        private val producers: KafkaProducers,
        private val consumers: KafkaConsumers,
        private val transactionHolder: ITransactionHolder,
        private val config: MQTransactionConfig,
        private val partitionProvider: IMqtsKafkaPartitionProvider,
        override val ordered: Int = 0) : DisposableBean, ITransactionQueue, IKafkaRecordHandler {

    companion object{
        private val logger by lazy { LoggerFactory.getLogger(KafkaQueue::class.java) }
    }

    private var queueHandlers: ConcurrentHashMap<String, ConcurrentHashMap<String, MutableSet<(transaction: MQTransaction) -> Unit>>> = ConcurrentHashMap()

    @Volatile
    private var isStarted: Boolean = false

    override var type: IKafkaRecordHandler.HandlerType = IKafkaRecordHandler.HandlerType.Participant


    private val stringSerializer: StringSerializer = StringSerializer()
    private val longSerializer: LongSerializer = LongSerializer()
    private val stringDeserializer: StringDeserializer = StringDeserializer()
    private val longDeserializer: LongDeserializer = LongDeserializer()

    override val name: String = "kafka"

    override fun destroy() {
        isStarted = false
        this.producers.close()
    }

    private fun fromKafkaMessage(record: ConsumerRecord<Long, ByteArray>): MQTransaction {
        val parentHeader = record.headers().lastHeader("mqts-pnt")

        val idHeader = record.headers().lastHeader("mqts-tid")
        //兼容旧版
        val tid = if(idHeader != null) this.longDeserializer.deserialize(record.topic(), idHeader.value()) else record.key()
        val mqTransaction = MQTransaction(
                transactionId = tid,
                transactionType = record.topic().removePrefix(Constants.TRANSACTION_TOPIC_PREFIX),
                timeCreated = record.timestamp(),
                timeExpired = this.longDeserializer.deserialize(record.topic(), record.headers().lastHeader("mqts-exp").value()),
                data = record.value(),
                ackHostAndPort = this.stringDeserializer.deserialize(record.topic(), record.headers().lastHeader("mqts-addr").value()),
                parentTransactionId = if (parentHeader != null) this.longDeserializer.deserialize(record.topic(), parentHeader.value()) else null
        )
        mqTransaction.version = this.stringDeserializer.deserialize(record.topic(), record.headers().lastHeader("mqts-ver").value())
        if(record.timestampType() != TimestampType.NO_TIMESTAMP_TYPE) {
            mqTransaction.states[MQTransaction.PRODUCE_TIME_STATE_KEY] = record.timestamp().toString()
            mqTransaction.states[MQTransaction.CONSUME_TIME_STATE_KEY] = (record.timestamp() + 1).coerceAtLeast(System.currentTimeMillis()).toString()
        }

        record.headers().forEach {
            if (it.key().startsWith("mqts-sta-")) {
                mqTransaction.states[it.key().removePrefix("mqts-sta-")] = this.stringDeserializer.deserialize(record.topic(), it.value())
            }
        }
        return mqTransaction
    }

    private fun toKafkaMessage(transaction: MQTransaction): ProducerRecord<Long?, ByteArray> {
        val data = transaction.data ?: ByteArray(0)

        val record = ProducerRecord<Long?, ByteArray>(
                getTopicFromTransactionType(transaction.transactionType),
                partitionProvider.getTransactionPartition(transaction),
                System.currentTimeMillis(),
                null,
                data)

        record.headers().add("mqts-tid", this.longSerializer.serialize(record.topic(), transaction.transactionId))
        record.headers().add("mqts-ver", this.stringSerializer.serialize(record.topic(), transaction.version))
        record.headers().add("mqts-exp", this.longSerializer.serialize(record.topic(), transaction.timeExpired))
        record.headers().add("mqts-addr", this.stringSerializer.serialize(record.topic(), transaction.ackHostAndPort))
        if (transaction.parentTransactionId != null) {
            record.headers().add("mqts-pnt", this.longSerializer.serialize(record.topic(), transaction.parentTransactionId))
        }
        transaction.extractStatesWithoutPayload().forEach {
            record.headers().add("mqts-sta-${it.key}", this.stringSerializer.serialize(record.topic(), it.value))
        }

        return record
    }


    private fun findHandlers(queue: String, topic: String): Set<(transaction: MQTransaction) -> Unit>? {
        val set = this.queueHandlers.getOrDefault(queue, null)
        return set?.getOrDefault(topic, null)
    }


    override fun registerHandler(queue: String, transactionType: String, handler: (transaction: MQTransaction) -> Unit) {
        if (isStarted) {
            throw MQTransactionException("The mqts handler can no longer be registered after the kafka queue started")
        }
        if(transactionType.endsWith(Constants.ACK_TOPIC_STUFFIX) || transactionType.endsWith(Constants.REDO_TOPIC_STUFFIX)){
            throw MQTransactionException("The mqts transaction type cant be end with '${Constants.ACK_TOPIC_STUFFIX}' or '${Constants.REDO_TOPIC_STUFFIX}'")
        }
        if (queue.isBlank()) {
            throw IllegalArgumentException("MQTS queue cant not be null or empty string.")
        }
        if (!config.queues.containsKey(queue)) {
            throw IllegalArgumentException("MQTS is missing configuration for queue '$queue'.")
        }
        if (transactionType.isBlank()) {
            throw IllegalArgumentException("MQTS transaction type cant not be null or empty string.")
        }
        val sourceTopic = getTopicFromTransactionType(transactionType)
        this.consumers.addTopic(queue, sourceTopic)

        val topics = this.queueHandlers.getOrPut(queue) {
            ConcurrentHashMap()
        }
        val handlers = topics.getOrPut(sourceTopic) {
            mutableSetOf<(transaction: MQTransaction) -> Unit>()
        }
        handlers.add(handler)
    }

    override fun send(queue: String, transaction: MQTransaction) {
        val producer = this.producers.get(queue)
        val message = this.toKafkaMessage(transaction)

        producer.send(message) { _, exception ->
            exception?.run {
                logger.error("Push mqts message fault.", this)
                exception.throwIfNecessary()
            }
        }
    }

    override fun handle(queue: String, record: ConsumerRecord<Long, ByteArray>) {
        val transaction = fromKafkaMessage(record)
        this.transactionHolder.scopeWith(transaction).use {
            this.findHandlers(queue, record.topic())?.forEach { handler ->
                handler.invoke(transaction)
            }
        }
    }
}