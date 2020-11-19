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
class AckServerStartup : IMQTransactionStartup {

    companion object{
        private val logger by lazy { LoggerFactory.getLogger(AckServerStartup::class.java) }
    }

    override fun getOrder(): Int = (Int.MAX_VALUE - 100)

    override fun start(applicationContext: ApplicationContext) {
        val mqTransactionManager = applicationContext.getBean(MQTransactionManager::class.java)
        val ackServer = applicationContext.getBean(IAckServer ::class.java)

        ackServer.startup(mqTransactionManager.transactionSources.toList())
    }
}
