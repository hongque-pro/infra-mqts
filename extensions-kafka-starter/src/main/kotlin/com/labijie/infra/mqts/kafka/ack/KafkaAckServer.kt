package com.labijie.infra.mqts.kafka.ack

import com.labijie.infra.mqts.MQTransaction
import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.TransactionResult
import com.labijie.infra.mqts.TransactionSourceAttribute
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.ack.AckRequest
import com.labijie.infra.mqts.kafka.Constants
import com.labijie.infra.mqts.kafka.IKafkaRecordHandler
import com.labijie.infra.mqts.kafka.KafkaConsumers
import com.labijie.infra.mqts.kafka.queue.getAckTopicFromTransactionType
import com.labijie.infra.utils.throwIfNecessary
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.record.TimestampType
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import java.net.URI
import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
class KafkaAckServer(private val applicationContext: ApplicationContext,
                     private val consumers: KafkaConsumers,
                     override val ordered: Int = 0) : IAckServer, IKafkaRecordHandler {
    companion object {
        private val logger by lazy { LoggerFactory.getLogger(KafkaAckServer::class.java) }
    }

    override val name: String = "kafka"

    override fun getAckAddress(): URI {
        return URI("kafka://default")
    }

    private var transactionManager: MQTransactionManager? = null
    private val interceptors: MutableSet<(context: AckRequest) -> Unit> = mutableSetOf()

    override var type: IKafkaRecordHandler.HandlerType = IKafkaRecordHandler.HandlerType.Ack

    fun registerInterceptor(interceptor: (context: AckRequest) -> Unit) {
        interceptors.add(interceptor)
    }

    override fun startup(sources: Collection<TransactionSourceAttribute>) {

        sources.forEach {
            consumers.addTopic(it.annotation.queue, getAckTopicFromTransactionType(it.annotation.type))
        }
    }

    override fun shutdown() {
        //nothing need to do
    }

    override fun handle(queue: String, record: ConsumerRecord<Long, ByteArray>) {
        this.transactionManager = transactionManager
                ?: this.applicationContext.getBean(MQTransactionManager::class.java)
        val (transactionId, result) = getMessage(record.value())

        val request = processInterceptor(transactionId, record)

        this.transactionManager!!.completeTransaction(queue, request, result)
    }


    private fun processInterceptor(transactionId: Long, record: ConsumerRecord<Long, ByteArray>): AckRequest {
        val states = record.headers().map {
            it.key() to it.value().toString(Charsets.UTF_8)
        }.toMap().toMutableMap()

        if(record.timestampType() != TimestampType.NO_TIMESTAMP_TYPE) {
            states[MQTransaction.PRODUCE_TIME_STATE_KEY] = record.timestamp().toString()
            states[MQTransaction.CONSUME_TIME_STATE_KEY] = (record.timestamp() + 1).coerceAtLeast(System.currentTimeMillis()).toString()
        }

        val transactionType = record.topic().removePrefix(Constants.TRANSACTION_TOPIC_PREFIX).removeSuffix(Constants.ACK_TOPIC_STUFFIX)

        val request = AckRequest(transactionId, transactionType, states)

        this.interceptors.forEach {
            try {
                it.invoke(request)
            } catch (ex: Throwable) {
                logger.error("Invoke ack server interceptor fault.", ex)
                ex.throwIfNecessary()
            }
        }
        return request
    }

    private fun getMessage(bytes: ByteArray): Pair<Long, TransactionResult> {
        val resultByte = (bytes[0].toInt())
        val idBytes = ByteBuffer.wrap(bytes)

        val result = if (resultByte == 1) TransactionResult.Committed else TransactionResult.Canceled
        val id = idBytes.getLong(1)

        return Pair(id, result)
    }
}