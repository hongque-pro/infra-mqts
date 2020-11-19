package com.labijie.infra.mqts.spring

import org.springframework.context.ApplicationContext
import org.springframework.core.Ordered

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
interface IMQTransactionStartup: Ordered {
    override fun getOrder(): Int = 0
    fun start(applicationContext: ApplicationContext)
}