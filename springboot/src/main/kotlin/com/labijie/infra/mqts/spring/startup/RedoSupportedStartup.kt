package com.labijie.infra.mqts.spring.startup

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.ITransactionRedoSupported
import com.labijie.infra.mqts.spring.IMQTransactionStartup
import org.springframework.context.ApplicationContext

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
class RedoSupportedStartup:IMQTransactionStartup {

    override fun getOrder(): Int = (Int.MAX_VALUE - 99)

    override fun start(applicationContext: ApplicationContext) {
        val transactionManager = applicationContext.getBean(MQTransactionManager::class.java)
        val redoSupported = applicationContext.getBean(ITransactionRedoSupported::class.java)
        val sources = transactionManager.transactionSources

        redoSupported.start(sources)
    }
}