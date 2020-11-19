package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.MQTransactionException
import com.labijie.infra.mqts.ack.AckRequestContext
import com.labijie.infra.mqts.TransactionResult

interface IAckClient {

    @Throws(MQTransactionException::class)
    fun write(context: AckRequestContext, result:TransactionResult)
}