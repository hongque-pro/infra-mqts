package com.labijie.infra.mqts.impl

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.TransactionResult
import com.labijie.infra.mqts.abstractions.IAckClient
import com.labijie.infra.mqts.ack.AckRequest
import com.labijie.infra.mqts.ack.AckRequestContext

class MemoryAckClient(
        private val mqTransactionManager: MQTransactionManager) : IAckClient {
    override fun write(context: AckRequestContext, result: TransactionResult) {
        val request = AckRequest(context.transactionId, context.transactionType, context.transactionStates.toMutableMap())
        mqTransactionManager.completeTransaction(context.queue, request, result)
    }
}