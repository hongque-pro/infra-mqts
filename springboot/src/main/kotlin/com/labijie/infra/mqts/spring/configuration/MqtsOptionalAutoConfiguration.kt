package com.labijie.infra.mqts.spring.configuration

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.abstractions.ITransactionHolder
import com.labijie.infra.mqts.abstractions.ITransactionQueue
import com.labijie.infra.mqts.abstractions.ITransactionRedoSupported
import com.labijie.infra.mqts.impl.*
import com.labijie.infra.mqts.spring.condition.ConditionalOnMqts
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-27
 */
@ConditionalOnMqts
@AutoConfigureAfter(MQTransactionManager::class)
@AutoConfigureOrder(Int.MAX_VALUE)
@Configuration(proxyBeanMethods = false)
class MqtsOptionalAutoConfiguration {

    @ConditionalOnMissingBean(ITransactionRedoSupported::class)
    @Bean
    fun noopTransactionRedoSupported(): ITransactionRedoSupported {
        return NoopTransactionRedoSupported()
    }

    @ConditionalOnMissingBean(ITransactionQueue::class)
    @Bean
    fun memoryTransactionQueue(transactionHolder: ITransactionHolder): MemoryTransactionQueue {
        return MemoryTransactionQueue(transactionHolder)
    }

    @ConditionalOnMissingBean(IAckServer::class)
    @Bean
    fun memoryAckServer(): MemoryAckServer = MemoryAckServer()

    @ConditionalOnBean(MemoryAckServer::class)
    @Bean
    fun memoryAckClient(mqTransactionManager: MQTransactionManager): MemoryAckClient = MemoryAckClient(mqTransactionManager)
}