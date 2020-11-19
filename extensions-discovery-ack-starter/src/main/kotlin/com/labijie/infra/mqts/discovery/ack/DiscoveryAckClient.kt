package com.labijie.infra.mqts.discovery.ack

import feign.Client
import feign.Contract
import feign.Feign
import feign.codec.Decoder
import feign.codec.Encoder
import com.labijie.infra.mqts.TransactionResult
import com.labijie.infra.mqts.abstractions.IAckClient
import com.labijie.infra.mqts.ack.AckRequestContext
import com.labijie.infra.mqts.discovery.MQTransactionDiscoveryException
import com.labijie.infra.mqts.discovery.web.AckModel
import com.labijie.infra.mqts.discovery.web.IAckEndpoint

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
class DiscoveryAckClient(
        private val decoder: Decoder,
        private val encoder: Encoder,
        private val client: Client,
        private val contract: Contract) : IAckClient {

    private val feignClients: MutableMap<String, IAckEndpoint> = mutableMapOf()

    @Throws(MQTransactionDiscoveryException::class)
    override fun write(context: AckRequestContext, result: TransactionResult) {
        val url = context.ackAddress
        if (url.scheme != "discovery") {
            throw MQTransactionDiscoveryException("MQTS ack client '${DiscoveryAckClient::class.java.name}' unsupported protocol ( protocol: ${url.scheme} ).")
        }
        val client = getFeignClient(context)
        try {
            client.sendAck(AckModel(result, context.queue, context.transactionId, context.transactionType, context.transactionStates))
        } catch (ex: Throwable) {
            throw MQTransactionDiscoveryException("MQTS ack request fault.", ex)
        }
    }

    private fun getFeignClient(context: AckRequestContext): IAckEndpoint {
        val serviceName = context.ackAddress.host
        return feignClients.getOrPut(serviceName) {
            Feign.builder().client(this.client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .target(IAckEndpoint::class.java, "http://$serviceName")
        }
    }
}