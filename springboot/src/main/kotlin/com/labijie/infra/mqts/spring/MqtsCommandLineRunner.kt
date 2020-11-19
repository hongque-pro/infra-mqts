package com.labijie.infra.mqts.spring

import com.labijie.infra.mqts.MQTransactionException
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.Ordered

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */
class MqtsCommandLineRunner : CommandLineRunner, ApplicationContextAware, Ordered {
    companion object{
        private val logger = LoggerFactory.getLogger(MqtsCommandLineRunner::class.java)
    }

    override fun getOrder(): Int {
        return Int.MIN_VALUE
    }

    private var applicationContext: ApplicationContext? = null

    override fun setApplicationContext(context: ApplicationContext) {
        this.applicationContext = context
    }

    override fun run(vararg args: String?) {
       val ctx = this.applicationContext ?: throw MQTransactionException("Spring context was not ready.")
        ctx.runMqts()
    }
}