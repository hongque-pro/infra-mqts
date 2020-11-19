package com.labijie.infra.mqts.spring

import com.labijie.infra.mqts.MQTransactionException
import com.labijie.infra.mqts.abstractions.IInstanceFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import kotlin.reflect.KClass

open class SpringInstanceFactory : IInstanceFactory, ApplicationContextAware {
    private var springContext:ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.springContext = applicationContext
    }


    override fun <T : Any> createInstance(clazz: KClass<T>): T {
        springContext ?: throw MQTransactionException("spring context is null, maybe it because spring has not yet loaded")
        return this.springContext!!.getBean(clazz.java);
    }
}