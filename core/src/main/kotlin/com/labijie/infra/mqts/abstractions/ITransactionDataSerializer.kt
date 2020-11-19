package com.labijie.infra.mqts.abstractions

import kotlin.reflect.KClass

interface ITransactionDataSerializer {
    fun serialize(data:Any):ByteArray
    fun <T:Any> deserialize(bytes:ByteArray, clazz: KClass<T>):T
}