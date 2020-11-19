package com.labijie.infra.mqts.kafka.redo

import com.labijie.infra.mqts.TransactionSourceAttribute
import com.labijie.infra.mqts.abstractions.ITransactionRedoSupported
import com.labijie.infra.mqts.kafka.KafkaConsumers
import com.labijie.infra.mqts.kafka.KafkaProducers
import com.labijie.infra.mqts.kafka.queue.getRedoTopicFromTransactionType
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.LongSerializer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-26
 */
class KafkaTransactionRedoSupported(private val producers: KafkaProducers, private val consumers: KafkaConsumers) : ITransactionRedoSupported {


    override fun start(transactionSources: Iterable<TransactionSourceAttribute>) {
        transactionSources.forEach {
            val redoTopic = getRedoTopicFromTransactionType(it.annotation.type)
            consumers.addTopic(it.annotation.queue, redoTopic)
        }
    }

    override fun redoTransaction(queue:String, transactionId: Long, transactionType: String) {
        val topic = getRedoTopicFromTransactionType(transactionType)
        val record = ProducerRecord<Long?, ByteArray>(topic, null, LongConverter.writeLong(transactionId))
        this.producers.get(queue).send(record)
    }
}