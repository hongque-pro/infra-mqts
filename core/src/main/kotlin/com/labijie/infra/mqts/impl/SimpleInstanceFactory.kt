package com.labijie.infra.mqts.impl

import com.labijie.infra.mqts.abstractions.IInstanceFactory
import kotlin.reflect.KClass

class SimpleInstanceFactory : IInstanceFactory {
    override fun <T : Any> createInstance(clazz: KClass<T>): T {
        return clazz.java.newInstance()
    }
}