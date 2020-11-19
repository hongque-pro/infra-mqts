package com.labijie.infra.mqts.impl

import com.labijie.infra.mqts.AckContext
import com.labijie.infra.mqts.abstractions.ITransactionAckCallback

class NoopTransactionAckCallback : ITransactionAckCallback {
    override fun onAck(context: AckContext) {
        context.commitMQTransaction()
    }
}