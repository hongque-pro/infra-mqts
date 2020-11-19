package com.labijie.infra.mqts.impl

import com.labijie.infra.json.JacksonHelper
import com.labijie.infra.mqts.abstractions.ITransactionDataSerializer
import kotlin.reflect.KClass

class JacksonDataSerializer : ITransactionDataSerializer {

    override fun serialize(data: Any): ByteArray {
        return JacksonHelper.serialize(data)
    }

    override fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T {
        return JacksonHelper.deserialize(bytes, clazz)
    }
}