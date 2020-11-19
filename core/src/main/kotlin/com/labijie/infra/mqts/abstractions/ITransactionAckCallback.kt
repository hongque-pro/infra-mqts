package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.AckContext

interface ITransactionAckCallback {
    fun onAck(context: AckContext)
}