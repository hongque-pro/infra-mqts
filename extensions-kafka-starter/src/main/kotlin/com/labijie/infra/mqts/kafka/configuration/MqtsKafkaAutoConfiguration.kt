package com.labijie.infra.mqts.kafka.configuration

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.abstractions.ITransactionHolder
import com.labijie.infra.mqts.abstractions.ITransactionRedoSupported
import com.labijie.infra.mqts.configuration.MQTransactionConfig
import com.labijie.infra.mqts.kafka.IKafkaRecordHandler
import com.labijie.infra.mqts.kafka.KafkaConsumers
import com.labijie.infra.mqts.kafka.KafkaProducers
import com.labijie.infra.mqts.kafka.KafkaStartup
import com.labijie.infra.mqts.kafka.ack.KafkaAckClient
import com.labijie.infra.mqts.kafka.ack.KafkaAckServer
import com.labijie.infra.mqts.kafka.queue.IMqtsKafkaPartitionProvider
import com.labijie.infra.mqts.kafka.queue.KafkaQueue
import com.labijie.infra.mqts.kafka.queue.NoneMqtsKafkaPartitionProvider
import com.labijie.infra.mqts.kafka.redo.KafkaTransactionRedoSupported
import com.labijie.infra.mqts.kafka.redo.TransactionRedoHandler
import com.labijie.infra.mqts.spring.condition.ConditionalOnMqts
import com.labijie.infra.mqts.spring.configuration.MqtsAutoConfiguration
import com.labijie.infra.mqts.spring.configuration.MqtsOptionalAutoConfiguration
import com.labijie.infra.spring.configuration.getApplicationName
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-19
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMqts
@AutoConfigureAfter(MqtsAutoConfiguration::class)
@AutoConfigureBefore(MqtsOptionalAutoConfiguration::class)
class MqtsKafkaAutoConfiguration {

    @Bean
    fun kafkaProducers(applicationContext: ApplicationContext, config: MQTransactionConfig): KafkaProducers {
        return KafkaProducers(applicationContext.applicationName, config)
    }

    @Bean
    fun kafkaConsumers(context: ApplicationContext,
                       environment: Environment,
                       config: MQTransactionConfig,
                       @Value("\${infra.mqts.kafka.redo-enabled:true}")
                       redoEnable: Boolean,
                       @Value("\${infra.mqts.kafka.ack-enabled:true}")
                       ackEnabled: Boolean
    ): KafkaConsumers {
//        val redoEnable = redoEnableString.ifNullOrBlank("")!! != "false"
//        val ackEnabled = redoEnableString.ifNullOrBlank("")!! != "false"
        val isDevelopment = environment.activeProfiles.contains("dev") || environment.activeProfiles.contains("local")
        val applicationName = environment.getApplicationName()
        return KafkaConsumers(applicationName, config, isDevelopment, ackEnabled, redoEnable)
    }

    @Bean
    @ConditionalOnMissingBean(IMqtsKafkaPartitionProvider::class)
    fun mqtsNoneKafkaPartitionProvider(): IMqtsKafkaPartitionProvider {
        return NoneMqtsKafkaPartitionProvider()
    }

    @Bean
    fun kafkaQueue(
            producers: KafkaProducers,
            consumers: KafkaConsumers,
            transactionHolder: ITransactionHolder,
            config: MQTransactionConfig,
            partitionProvider: IMqtsKafkaPartitionProvider): KafkaQueue {
        return KafkaQueue(producers, consumers, transactionHolder, config, partitionProvider)
    }

    @Bean
    fun kafkaStartup(consumers: KafkaConsumers): KafkaStartup {
        return KafkaStartup(consumers)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "infra.mqts.kafka", name = ["ack-enabled"], matchIfMissing = true)
    protected class MqtsKafkaAckAutoConfiguration {
        @Bean
        fun kafkaAckServer(applicationContext: ApplicationContext, consumers: KafkaConsumers): KafkaAckServer {
            return KafkaAckServer(applicationContext, consumers)
        }

        @Bean
        fun kafkaAckClient(producers: KafkaProducers): KafkaAckClient {
            return KafkaAckClient(producers)
        }
    }


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "infra.mqts.kafka", name = ["redo-enabled"], matchIfMissing = true)
    protected class MqtsKafkaRedoSupportedAutoConfiguration {
        @Bean
        fun transactionRedoHandler(ackServer: IAckServer, mqTransactionManager: MQTransactionManager): IKafkaRecordHandler {
            return TransactionRedoHandler(ackServer, mqTransactionManager)
        }

        @Bean
        fun kafkaTransactionRedoSupported(kafkaProducers: KafkaProducers, kafkaConsumers: KafkaConsumers): ITransactionRedoSupported {
            return KafkaTransactionRedoSupported(kafkaProducers, kafkaConsumers)
        }
    }
}