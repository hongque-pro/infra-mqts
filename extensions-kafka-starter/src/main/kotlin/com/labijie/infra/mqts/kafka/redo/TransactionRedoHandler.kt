package com.labijie.infra.mqts.kafka.redo

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.context.TransactionContext
import com.labijie.infra.mqts.kafka.IKafkaRecordHandler
import com.labijie.infra.mqts.kafka.queue.getTransactionTypeFromRedoTopic
import com.labijie.infra.utils.toLong
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-26
 */
class TransactionRedoHandler(
        private val ackServer: IAckServer,
        private val transactionManager: MQTransactionManager) : IKafkaRecordHandler {
    companion object{
        private val logger by lazy { LoggerFactory.getLogger(TransactionRedoHandler::class.java) }
    }

    override var type: IKafkaRecordHandler.HandlerType = IKafkaRecordHandler.HandlerType.Redo
    override val ordered:Int = 0

    override fun handle(queue: String, record: ConsumerRecord<Long, ByteArray>) {
        val transactionType = getTransactionTypeFromRedoTopic(record.topic())
        val transactionId = record.value().toLong()

        val transaction = this.transactionManager.recoverExpiredTransaction(transactionId, transactionType)
        if (transaction == null) {
            logger.warn("MQTS cant find expired transaction with id '$transactionId' for redo operation.")
        } else {
            val parentId = transaction.parentTransactionId
            val parent = if(parentId != null){
                this.transactionManager.findTransaction(parentId)
            } else null
            this.transactionManager.beginTransaction(ackServer, TransactionContext(transaction, parent), null)
            logger.info("MQTS transaction '$transactionId' has been redo.")
        }
    }
}