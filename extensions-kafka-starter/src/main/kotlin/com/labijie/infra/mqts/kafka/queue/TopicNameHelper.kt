package com.labijie.infra.mqts.kafka.queue

import com.labijie.infra.mqts.MQTransactionException
import com.labijie.infra.mqts.kafka.Constants
import com.labijie.infra.mqts.kafka.IKafkaRecordHandler
import com.labijie.infra.utils.ifNullOrBlank

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-19
 */

internal fun getTopicFromTransactionType(transactionType: String): String =
        "${Constants.TRANSACTION_TOPIC_PREFIX}${transactionType.ifNullOrBlank("default")!!.trim()}"

internal fun getRedoTopicFromTransactionType(transactionType: String): String =
        "${Constants.TRANSACTION_TOPIC_PREFIX}${transactionType.ifNullOrBlank("default")!!.trim()}${Constants.REDO_TOPIC_STUFFIX}"

internal fun getAckTopicFromTransactionType(transactionType: String): String =
        "${Constants.TRANSACTION_TOPIC_PREFIX}${transactionType.ifNullOrBlank("default")!!.trim()}${Constants.ACK_TOPIC_STUFFIX}"

internal fun getTransactionTypeFromRedoTopic(topic: String): String {
    return topic.removePrefix(Constants.TRANSACTION_TOPIC_PREFIX).removeSuffix(Constants.REDO_TOPIC_STUFFIX)
}

internal fun getHandlerType(topic: String): IKafkaRecordHandler.HandlerType {
    if (topic.startsWith(Constants.TRANSACTION_TOPIC_PREFIX)) {
        return if (topic.endsWith(Constants.ACK_TOPIC_STUFFIX)) IKafkaRecordHandler.HandlerType.Ack else IKafkaRecordHandler.HandlerType.Participant
    }
    throw MQTransactionException("Topic '$topic' is not a MQTS topic.")
}