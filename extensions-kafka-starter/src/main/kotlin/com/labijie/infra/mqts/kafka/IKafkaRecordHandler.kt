package com.labijie.infra.mqts.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
interface IKafkaRecordHandler {
    val ordered: Int
//    fun canHandle(queue: String, record: ConsumerRecord<Long, ByteArray>): Boolean
    fun handle(queue: String, record: ConsumerRecord<Long, ByteArray>): Unit
    var type:HandlerType

    enum class HandlerType{
        Participant,
        Ack,
        Redo
    }
}