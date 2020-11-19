package com.labijie.infra.mqts.spring

import com.labijie.infra.mqts.abstractions.IAckClient
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.abstractions.ITransactionQueue
import com.labijie.infra.mqts.impl.MemoryAckServer
import com.labijie.infra.mqts.impl.MemoryTransactionQueue
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import java.util.stream.Collectors
import kotlin.system.exitProcess

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-16
 */

private val logger = LoggerFactory.getLogger("com.labijie.infra.mqts")

fun ApplicationContext.runMqts(exitProcessIfFault: Boolean = true): Unit {
    try {
        checkMqts()
        val startupList = this.getBeanProvider(IMQTransactionStartup::class.java).orderedStream().collect(Collectors.toList())
        startupList.forEach { t -> t.start(this) }
        logger.info("MQTS has been fully started successfully.")
    } catch (ex: Throwable) {
        logger.error("Error occurred when starting the mqts.", ex)
        if (exitProcessIfFault) exitProcess(-23165) else throw ex
    }
}

private fun ApplicationContext.checkMqts() {
    val queueProvider = this.getBean(ITransactionQueue::class.java)
    if (queueProvider is MemoryTransactionQueue) {
        logger.warn("Memory queue was used for mqts, it is for debugging, testing only, don't use in production environment !!!")
    }

    val ackServer = this.getBean(IAckServer::class.java)
    if (ackServer is MemoryAckServer) {
        logger.warn("Memory ack server and client were used for mqts, it is for debugging, testing only,  don't use in production environment !!!")
    }
}