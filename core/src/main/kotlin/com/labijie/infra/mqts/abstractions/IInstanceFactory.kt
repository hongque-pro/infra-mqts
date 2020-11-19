package com.labijie.infra.mqts.abstractions

import kotlin.reflect.KClass

interface IInstanceFactory {
    fun <T : Any> createInstance(clazz:KClass<T>) : T;
}