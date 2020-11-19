package com.labijie.infra.mqts.context

import com.labijie.infra.mqts.MQTransaction

interface ITransactionContext {
    val transaction: MQTransaction
    val parentTransaction: MQTransaction?
    val states: MutableMap<String, Any>
}

interface ITransactionInitializationContext : ITransactionContext {
    val methodReturnValue: Any?
}

interface ITransactionCompletionContext : ITransactionContext {
    var idempotent:Boolean
    var exception: Throwable?
    val hasError: Boolean
        get() = (exception != null)
}