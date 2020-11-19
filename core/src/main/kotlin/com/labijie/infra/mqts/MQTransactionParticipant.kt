package com.labijie.infra.mqts

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MQTransactionParticipant(
        val type:String,
        val queue:String,
        val autoIdempotent: Boolean = true)

interface IParticipantInfo{
    val type:String
    val autoIdempotent: Boolean
    val queue:String
}

fun MQTransactionParticipant.info(): IParticipantInfo {
    val annotation = this
    return object : IParticipantInfo{
        override val type: String = annotation.type
        override val autoIdempotent: Boolean = annotation.autoIdempotent
        override val queue: String = annotation.queue
    }
}