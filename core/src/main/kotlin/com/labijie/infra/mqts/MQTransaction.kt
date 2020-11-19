package com.labijie.infra.mqts

class MQTransaction(
    var transactionId: Long = System.currentTimeMillis(),
    var transactionType: String = "default",
    var timeCreated: Long = System.currentTimeMillis(),
    var timeExpired:Long = Long.MAX_VALUE,
    var data: ByteArray? = null,
    var ackHostAndPort: String = "",
    var parentTransactionId: Long? = null){

    companion object {
        const val  PRODUCE_TIME_STATE_KEY = "__time_produced"
        const val  CONSUME_TIME_STATE_KEY = "__time_consumed"
    }

    var version = "1.0.0"

    var states:HashMap<String,String> = HashMap()

    fun copy():MQTransaction{
        val map = HashMap<String, String>(this.states.size)
        this.states.forEach{
            map[it.key] = it.value
        }
        val transaction = MQTransaction(this.transactionId,
                this.transactionType,
                this.timeCreated,
                this.timeExpired,
                this.data,
                this.ackHostAndPort,
                this.parentTransactionId)
        transaction.version = version
        transaction.states = map

        return transaction
    }

    fun extractStatesWithoutPayload():Map<String, String>{
        val map = mutableMapOf<String, String>()
        this.states.forEach{
            if(!it.key.startsWith(MQTransactionManager.PAYLOAD_STATE_PREFIX)){
                map.put(it.key, it.value)
            }
        }

        return map.toMap()
    }

    fun extractPayload(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        this.states.forEach{
            if(it.key.startsWith(MQTransactionManager.PAYLOAD_STATE_PREFIX)){
                val actualKey=it.key.substring(MQTransactionManager.PAYLOAD_STATE_PREFIX.length)
                map.put(actualKey, it.value)
            }
        }

        return map.toMap()
    }

}