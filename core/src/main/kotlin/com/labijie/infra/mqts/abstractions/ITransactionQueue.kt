package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.MQTransaction

interface ITransactionQueue: INamedComponent {
    fun send(queue: String, transaction: MQTransaction): Unit
    fun registerHandler(queue: String, transactionType: String, handler: (transaction: MQTransaction) -> Unit): Unit
}