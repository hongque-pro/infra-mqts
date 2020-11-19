package com.labijie.infra.mqts.spring

import com.labijie.infra.mqts.MQTransactionManager
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-16
 */
class MQTransactionApplicationListener(private val mqTransactionManager: MQTransactionManager) : ApplicationListener<ContextClosedEvent> {
    override fun onApplicationEvent(event: ContextClosedEvent) {
        this.mqTransactionManager.stopAllRetry()
    }
}