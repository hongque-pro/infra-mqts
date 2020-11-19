package com.labijie.infra.mqts.impl

import com.labijie.infra.mqts.MQTransaction
import com.labijie.infra.mqts.abstractions.ITransactionHolder

class DefaultTransactionHolder:ITransactionHolder {
    private val state:InheritableThreadLocal<MQTransaction> = InheritableThreadLocal()

    override var currentTransaction: MQTransaction?
        get() = this.state.get()
        set(value) {
            if (value != null) state.set(value) else state.remove()
        }
}