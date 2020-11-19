package com.labijie.infra.mqts

class AckContext(private val mqTransaction: MQTransaction,
                 private val mqTransactionManager: MQTransactionManager,
                 val result: TransactionResult = TransactionResult.Committed,
                 private val commitHandler:()->Unit) {

    internal var isCommitted:Boolean = false

    val transactionId: Long
        get() = this.mqTransaction.transactionId

    @Synchronized
    @Throws(IdempotenceException::class)
    fun commitMQTransaction() {
        if(!isCommitted) {
            commitHandler()
            isCommitted = true
        }
    }

    fun extractPayload(): Map<String, String> {
        return this.mqTransaction.extractPayload()
    }
}
