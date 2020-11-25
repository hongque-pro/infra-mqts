package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.MQTransactionException
import com.labijie.infra.mqts.TransactionSourceAttribute
import com.labijie.infra.mqts.ack.AckRequest
import com.labijie.infra.mqts.ack.CallbackKey
import java.net.URI
import java.net.URL

interface IAckServer : INamedComponent {
    @Throws(MQTransactionException::class)
    fun startup(sources:Collection<TransactionSourceAttribute>)
    fun getAckAddress():URI
    fun shutdown()
}