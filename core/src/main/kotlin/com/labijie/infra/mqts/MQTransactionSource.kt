package com.labijie.infra.mqts

import com.labijie.infra.mqts.impl.NoopTransactionAckCallback
import kotlin.reflect.KClass


@Suppress("DEPRECATED_JAVA_ANNOTATION")
@java.lang.annotation.Repeatable(MQTransactionSources::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MQTransactionSource(
        val type: String,
        val queue: String,
        val ackCallback: KClass<out Any> = NoopTransactionAckCallback::class,
        val timeoutSeconds: Int = 5 * 60,
        val retryIntervalSeconds: IntArray = [10, 30, 60, 120])

interface ISourceInfo{
    val type: String
    val ackCallback: KClass<out Any>?
    val timeoutSeconds: Int get() = 5 * 60
    val queue: String
    val retryIntervalSeconds: IntArray get()= intArrayOf(10, 30, 60, 120)
}

fun MQTransactionSource.info(): ISourceInfo {
    val annotation = this
    return object : ISourceInfo{
        override val type: String = annotation.type
        override val ackCallback: KClass<out Any>? = if(annotation.ackCallback != NoopTransactionAckCallback::class) annotation.ackCallback else null
        override val timeoutSeconds: Int = annotation.timeoutSeconds
        override val queue: String = annotation.queue
        override val retryIntervalSeconds: IntArray = annotation.retryIntervalSeconds
    }
}