package com.labijie.infra.mqts.spring.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.*
import com.labijie.infra.mqts.configuration.MQTransactionConfig
import com.labijie.infra.mqts.impl.DefaultTransactionHolder
import com.labijie.infra.mqts.impl.JacksonDataSerializer
import com.labijie.infra.mqts.spring.MQTransactionApplicationListener
import com.labijie.infra.mqts.spring.SpringInstanceFactory
import com.labijie.infra.mqts.spring.condition.ConditionalOnMqts
import com.labijie.infra.mqts.spring.interceptor.Aspect.MQTransactionAspect
import com.labijie.infra.mqts.spring.processor.MqtsAnnotationProcessor
import com.labijie.infra.mqts.spring.startup.AckServerStartup
import com.labijie.infra.mqts.spring.startup.AnnotationDiscoveryStartup
import com.labijie.infra.mqts.spring.startup.RedoSupportedStartup
import com.labijie.infra.mqts.spring.startup.TransactionRecoveryStartup
import com.labijie.infra.spring.configuration.CommonsAutoConfiguration
import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role

@ConditionalOnMqts
@AutoConfigureAfter(CommonsAutoConfiguration::class)
@Configuration(proxyBeanMethods = false)
class MqtsAutoConfiguration {

    @ConfigurationProperties("infra.mqts")
    @Bean
    fun mqTransactionConfig(): MQTransactionConfig{
        return MQTransactionConfig()
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(IInstanceFactory::class)
    fun springInstanceFactory(): IInstanceFactory {
        return SpringInstanceFactory()
    }

    @Bean
    fun mqTransactionManager(config: MQTransactionConfig,
                             idGenerator: IIdGenerator,
                             dataSerializer: ITransactionDataSerializer,
                             idempotence: IIdempotence,
                             transactionAccessor: ITransactionHolder,
                             instanceFactory: IInstanceFactory,
                             queue: ITransactionQueue,
                             repository: ITransactionRepository,
                             networkConfig: NetworkConfig): MQTransactionManager {
        return MQTransactionManager(idempotence,
                repository,
                dataSerializer,
                idGenerator,
                config,
                instanceFactory,
                transactionAccessor,
                queue,
                networkConfig.getIPAddress())
    }

    @Bean
    fun mqTransactionAspect(applicationContext: ApplicationContext): MQTransactionAspect {
        return MQTransactionAspect(applicationContext)
    }

    @Bean
    fun mqTransactionApplicationListener(mqTransactionManager: MQTransactionManager): MQTransactionApplicationListener {
        return MQTransactionApplicationListener(mqTransactionManager)
    }

    @Bean
    @ConditionalOnMissingBean(ITransactionDataSerializer::class)
    fun transactionDataSerializer(): ITransactionDataSerializer {
        return JacksonDataSerializer()
    }

    @Bean
    @ConditionalOnMissingBean(ITransactionHolder::class)
    fun transactionHolder(): ITransactionHolder {
        return DefaultTransactionHolder()
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun mqtsAnnotationProcessor(config: MQTransactionConfig): MqtsAnnotationProcessor {
        return MqtsAnnotationProcessor(config)
    }

    @Configuration(proxyBeanMethods = false)
    @AutoConfigureAfter(MqtsAutoConfiguration::class)
    protected class CoreStartupAutoConfiguration {

        @Bean
        fun annotationDiscoveryStartup(): AnnotationDiscoveryStartup {
            return AnnotationDiscoveryStartup()
        }

        @Bean
        fun transactionRecoveryStartup(): TransactionRecoveryStartup {
            return TransactionRecoveryStartup()
        }

        @Bean
        fun ackServerStartup(): AckServerStartup {
            return AckServerStartup()
        }

        @Bean
        fun  redoSupportedStartup():RedoSupportedStartup{
            return RedoSupportedStartup()
        }
    }

}