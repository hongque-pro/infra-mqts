package com.labijie.infra.mqts.serialization

import com.esotericsoftware.kryo.Kryo
import com.labijie.infra.kryo.PooledKryo
import com.labijie.infra.mqts.MQTransaction

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
object KryoTransactionSerializer {
    private val kryo: PooledKryo = object: PooledKryo() {
        override fun createKryo(): Kryo {
            return Kryo().apply {
                this.register(HashMap::class.java, 10)
                this.register(MQTransaction::class.java, 11)
            }
        }
    }

    fun deserializeMQTransaction(bytes:ByteArray): MQTransaction {
        return this.kryo.deserialize(bytes, MQTransaction::class)
    }

    fun serializeMQTransaction(transaction:MQTransaction): ByteArray {
        return this.kryo.serialize(transaction)
    }
}