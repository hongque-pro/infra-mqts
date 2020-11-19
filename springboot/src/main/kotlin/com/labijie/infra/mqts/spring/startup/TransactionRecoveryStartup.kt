package com.labijie.infra.mqts.spring.startup

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.spring.IMQTransactionStartup
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-05
 */
class TransactionRecoveryStartup : IMQTransactionStartup {
    companion object{
        private val logger by lazy { LoggerFactory.getLogger(TransactionRecoveryStartup::class.java) }
    }

    override fun getOrder(): Int = Int.MAX_VALUE

    override fun start(applicationContext: ApplicationContext) {
        val ackServer = applicationContext.getBean(IAckServer::class.java)
        val mqTransactionManager = applicationContext.getBean(MQTransactionManager::class.java)
        val r = mqTransactionManager.recoveryTransactions(ackServer)
        logger.info("MQTS transactions have been fully recovered ( recovered: ${r.recovered}, expired: ${r.expired} ).")
    }
}