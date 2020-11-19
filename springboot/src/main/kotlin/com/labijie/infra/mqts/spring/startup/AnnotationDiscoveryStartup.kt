package com.labijie.infra.mqts.spring.startup

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.ITransactionListener
import com.labijie.infra.mqts.spring.IMQTransactionStartup
import com.labijie.infra.mqts.spring.processor.MqtsAnnotationProcessor
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
open class AnnotationDiscoveryStartup() : IMQTransactionStartup {
    companion object {
        private var isMQTransactionServiceStarted:Boolean = false

        val startupLock = Any()

        val logger by lazy { LoggerFactory.getLogger(AnnotationDiscoveryStartup::class.java) }
    }

    override fun getOrder(): Int = -1

    override fun start(applicationContext: ApplicationContext): Unit {
        if (!isMQTransactionServiceStarted) {
            synchronized(startupLock) {
                if (!isMQTransactionServiceStarted) {

                    val manager = applicationContext.getBean(MQTransactionManager::class.java)
                    val transactionQueueProcessor = applicationContext.getBean(MqtsAnnotationProcessor::class.java)
                    val sourceHandlers = transactionQueueProcessor.getSourceQueueHandlers()
                    val participantHandlers = transactionQueueProcessor.getParticipantQueueHandlers()

                    val listeners = applicationContext.getBeansOfType(ITransactionListener::class.java).values

                    manager.registerListeners(*listeners.toTypedArray())
                    if (sourceHandlers.isNotEmpty()) {
                        manager.enableSource(*sourceHandlers.toTypedArray())
                        //val address = manager.loadAckEndPoint()

                        //logger.info("MQTS source was found, use ack address endpoint: $address")
                    }
                    manager.enableParticipant(*participantHandlers.toTypedArray())

                    logger.info("MQTS annotation was loaded.")

                    isMQTransactionServiceStarted = true
                }
            }
        }
    }
}