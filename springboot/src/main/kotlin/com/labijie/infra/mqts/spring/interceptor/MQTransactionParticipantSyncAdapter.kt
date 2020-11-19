package com.labijie.infra.mqts.spring.interceptor

import com.labijie.infra.mqts.*
import com.labijie.infra.mqts.abstractions.IAckClient
import org.springframework.transaction.support.TransactionSynchronizationAdapter

class MQTransactionParticipantSyncAdapter(
        private val transaction: MQTransaction,
        private val ackClient: IAckClient,
        private val mqTransactionManager: MQTransactionManager,
        private val participant: IParticipantInfo)
    : TransactionSynchronizationAdapter() {

    var idempotence = false

    override fun afterCompletion(status: Int) {
        when (status) {
            STATUS_COMMITTED -> this.mqTransactionManager.ackTransaction(transaction, ackClient, participant, TransactionResult.Committed)
            STATUS_ROLLED_BACK -> if (idempotence) {
                this.mqTransactionManager.ackTransaction(transaction, ackClient, participant, TransactionResult.Committed)
            }
        }
    }

}