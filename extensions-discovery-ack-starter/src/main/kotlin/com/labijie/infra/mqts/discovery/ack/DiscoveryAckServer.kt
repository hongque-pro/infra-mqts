package com.labijie.infra.mqts.discovery.ack

import com.labijie.infra.mqts.TransactionSourceAttribute
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.ack.AckRequest
import java.net.URI

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
class DiscoveryAckServer(private val applicationName:String): IAckServer {

    val interceptors:MutableSet<(context: AckRequest) -> Unit> = mutableSetOf()

    override fun startup(sources: Collection<TransactionSourceAttribute>) {
    }

    fun registerInterceptor(interceptor: (context: AckRequest) -> Unit) {
        interceptors.add(interceptor)
    }

    override fun getAckAddress(): URI {
        return URI("discovery://$applicationName")
    }

    override fun shutdown() {
    }
}