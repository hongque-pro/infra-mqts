package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.MQTransaction
import com.labijie.infra.mqts.TransactionSourceAttribute

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-26
 */
interface ITransactionRedoSupported {

    fun redoTransaction(queue:String, transactionId: Long, transactionType: String): Unit {}

    fun start(transactionSources:Iterable<TransactionSourceAttribute>)
}