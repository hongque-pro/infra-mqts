package com.labijie.infra.mqts.kafka.ack

import com.labijie.infra.mqts.MQTransaction
import com.labijie.infra.mqts.ack.AckRequestContext
import com.labijie.infra.mqts.TransactionResult
import com.labijie.infra.mqts.abstractions.IAckClient
import com.labijie.infra.mqts.kafka.Constants
import com.labijie.infra.mqts.kafka.KafkaProducers
import org.apache.kafka.clients.producer.ProducerRecord
import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
class KafkaAckClient(private val producers: KafkaProducers) : IAckClient {
    override fun write(context: AckRequestContext, result: TransactionResult) {

        val producer = this.producers.get(context.queue)

        val record = ProducerRecord(
                "${Constants.TRANSACTION_TOPIC_PREFIX}${context.transactionType}${Constants.ACK_TOPIC_STUFFIX}",
                null,
                System.currentTimeMillis(),
                context.transactionId,
                createResultData(context.transactionId, result))

        context.transactionStates.forEach{
            if(!it.value.isBlank() && it.key != MQTransaction.PRODUCE_TIME_STATE_KEY && it.key != MQTransaction.CONSUME_TIME_STATE_KEY) {
                record.headers().add(it.key, it.value.toByteArray(Charsets.UTF_8))
            }
        }

        producer.send(record)
    }

    private fun createResultData(transactionId:Long, result: TransactionResult): ByteArray {

        val data = ByteArray(8 + 1)

        val resultByte = (if (result == TransactionResult.Committed) 0 else 1).toByte()
        val idBytes = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(transactionId).array()

        data[0] = resultByte
        System.arraycopy(idBytes, 0, data, 1, idBytes.size)

        return data
    }
}