package com.labijie.infra.mqts.context

import com.labijie.infra.mqts.MQTransaction


open class TransactionContextBase protected constructor(val transaction: MQTransaction, var parentTransaction: MQTransaction? = null) {
    constructor(context: TransactionContext) : this(context.transaction.copy(), context.parentTransaction?.copy()) {
    }

    val states: MutableMap<String, Any> = mutableMapOf()
}

class TransactionContext(transaction: MQTransaction, parentTransaction: MQTransaction? = null, returnValue: Any? = null)
    : TransactionContextBase(transaction, parentTransaction), ITransactionInitializationContext, ITransactionCompletionContext {

    override val methodReturnValue = returnValue

    override var idempotent: Boolean = false

    override var exception: Throwable? = null;
    override val hasError: Boolean
        get() = (exception != null)
}
