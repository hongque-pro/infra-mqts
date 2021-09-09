package com.labijie.infra.mqts

import com.labijie.infra.mqts.abstractions.ITransactionHolder
import com.labijie.infra.mqts.ack.AckRequest
import com.labijie.infra.mqts.ack.AckRequestContext
import com.labijie.infra.mqts.context.TransactionContext
import com.labijie.infra.mqts.impl.DefaultTransactionHolder
import com.labijie.infra.utils.throwIfNecessary
import java.net.URI


fun MQTransaction.isExpired(attribute: TransactionSourceAttribute): Boolean {
//    if(attribute.annotation.timeoutSeconds == Int.MAX_VALUE){
//        return false
//    }
//    val timeExpired = this.timeCreated + (attribute.annotation.timeoutSeconds * 1000)
    return this.timeExpired <= System.currentTimeMillis()
}

fun getNextRetrySeconds(retryCount: Int, tickArray: IntArray): Int {
    if(tickArray.isEmpty()){
        throw IllegalArgumentException("TickArray must be not null for getNextRetrySeconds method.")
    }
    return if (retryCount >= tickArray.size) tickArray[tickArray.size -1] else tickArray[Math.max(0, retryCount)]
}

fun ITransactionHolder.scopeWith(transaction: MQTransaction) : AutoCloseable {
    return TransactionScope(this, transaction)
}

private class TransactionScope(private val transactionHolder: ITransactionHolder, transaction: MQTransaction): AutoCloseable{
    init {
        transactionHolder.currentTransaction = transaction
    }
    override fun close() {
        transactionHolder.currentTransaction = null
    }

}





