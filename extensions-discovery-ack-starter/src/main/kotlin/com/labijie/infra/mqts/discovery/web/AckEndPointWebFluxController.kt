package com.labijie.infra.mqts.discovery.web

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.discovery.ack.DiscoveryAckServer
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono



/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class AckEndPointWebFluxController(
        private val mqTransactionManager: MQTransactionManager,
        private val discoveryAckServer: DiscoveryAckServer) {

    @PostMapping(Constants.AckWebPath)
    fun doAck(@RequestBody ackModel: AckModel): Mono<AckResponse> {
        this.discoveryAckServer.interceptors.forEach {
            it.invoke(ackModel)
        }
        this.mqTransactionManager.completeTransaction(ackModel.queue, ackModel, ackModel.result)
        return Mono.just(AckResponse.OK)
    }
}