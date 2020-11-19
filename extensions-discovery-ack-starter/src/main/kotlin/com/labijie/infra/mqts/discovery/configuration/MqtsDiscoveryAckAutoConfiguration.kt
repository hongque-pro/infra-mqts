package com.labijie.infra.mqts.discovery.configuration

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.discovery.ack.DiscoveryAckClient
import com.labijie.infra.mqts.discovery.ack.DiscoveryAckServer
import com.labijie.infra.mqts.discovery.web.AckEndPointController
import com.labijie.infra.mqts.discovery.web.AckEndPointWebFluxController
import com.labijie.infra.mqts.spring.condition.ConditionalOnMqts
import com.labijie.infra.mqts.spring.configuration.MqtsAutoConfiguration
import com.labijie.infra.mqts.spring.configuration.MqtsOptionalAutoConfiguration
import com.labijie.infra.spring.configuration.getApplicationName
import feign.Client
import feign.Contract
import feign.codec.Decoder
import feign.codec.Encoder
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
@Configuration
@ConditionalOnMqts
@AutoConfigureAfter(MqtsAutoConfiguration::class)
@AutoConfigureBefore(MqtsOptionalAutoConfiguration::class)
class MqtsDiscoveryAckAutoConfiguration {

    @Bean
    fun discoveryAckServer(environment: Environment): DiscoveryAckServer {
        return DiscoveryAckServer(environment.getApplicationName())
    }

    @Bean
    fun discoveryAckClient(decoder: Decoder,
                           encoder: Encoder,
                           client: Client,
                           contract: Contract): DiscoveryAckClient {
        return DiscoveryAckClient(decoder, encoder, client, contract)
    }


    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    fun ackEndPointController(mqTransactionManager: MQTransactionManager, discoveryAckServer: DiscoveryAckServer): AckEndPointController {
        return AckEndPointController(mqTransactionManager, discoveryAckServer)
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    fun ackEndPointWebFluxController(mqTransactionManager: MQTransactionManager, discoveryAckServer: DiscoveryAckServer): AckEndPointWebFluxController {
        return AckEndPointWebFluxController(mqTransactionManager, discoveryAckServer)
    }
}