package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.context.ITransactionCompletionContext
import com.labijie.infra.mqts.context.ITransactionContext
import com.labijie.infra.mqts.context.ITransactionInitializationContext

interface ITransactionListener {
    fun onTransactionPrepared(transactionScope: ITransactionInitializationContext)
    fun onTransactionStarting(transactionScope: ITransactionContext)
    fun onTransactionExpired(transactionScope: ITransactionContext)
    //fun onTransactionIdempotent(transactionScope: ITransactionContext)
    fun onTransactionStarted(transactionScope: ITransactionCompletionContext)

    fun onTransactionAckReceived(transactionScope: ITransactionContext)
    fun onTransactionCompleted(transactionScope: ITransactionCompletionContext)

    fun onTransactionExecuting(transactionScope: ITransactionContext)
    fun onTransactionExecuted(transactionScope: ITransactionCompletionContext)

    fun onTransactionAckRequesting(transactionScope: ITransactionContext)
    fun onTransactionAckRequested(transactionScope: ITransactionCompletionContext)


    fun onTransactionRetryStarting(transactionScope: ITransactionContext)
    fun onTransactionRetryStarted(transactionScope: ITransactionCompletionContext)
}