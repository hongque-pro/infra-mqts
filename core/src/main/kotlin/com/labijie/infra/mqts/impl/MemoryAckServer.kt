package com.labijie.infra.mqts.impl

import com.labijie.infra.mqts.TransactionSourceAttribute
import com.labijie.infra.mqts.abstractions.IAckServer
import java.net.URI

class MemoryAckServer : IAckServer {
    override fun startup(sources: Collection<TransactionSourceAttribute>) {
    }

    override fun getAckAddress(): URI = URI.create("memory://default")

    override fun shutdown() {
    }
}