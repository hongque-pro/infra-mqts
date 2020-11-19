package com.labijie.infra.mqts.spring.processor

import com.labijie.infra.mqts.*
import com.labijie.infra.mqts.configuration.MQTransactionConfig
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.BeanPostProcessor
import java.lang.reflect.Method
import kotlin.reflect.jvm.kotlinFunction

/**
 *
 * @author lishiwen
 * @date 18-7-30
 * @since JDK1.8
 */
class MqtsAnnotationProcessor(private val config: MQTransactionConfig) : BeanPostProcessor {

    private val participantQueueHandlers: MutableSet<Method> = mutableSetOf()
    private val sourceQueueHandlers: MutableSet<Method> = mutableSetOf()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val targetClass: Class<*> = AopUtils.getTargetClass(bean)
        targetClass.methods.forEach { method ->
            if (method.isAnnotationPresent(MQTransactionSources::class.java) ||
                    method.isAnnotationPresent(MQTransactionSource::class.java)) {
                sourceQueueHandlers.add(method)
            }

            if (method.isAnnotationPresent(MQTransactionParticipant::class.java))
                participantQueueHandlers.add(method)
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }

    fun getParticipantQueueHandlers() = participantQueueHandlers.toList()
            .map {
                val methodFullName = "${it.declaringClass.simpleName}.${it.name}"

                val annotation = it.getAnnotation(MQTransactionParticipant::class.java)

                if(annotation.queue.isBlank()){
                    throw IllegalArgumentException("MQTransactionParticipant missed queue property ( method: $methodFullName ) .")
                }
                if(annotation.type.isBlank()){
                    throw IllegalArgumentException("MQTransactionParticipant missed type property ( method: $methodFullName ) .")
                }

                 TransactionParticipantAttribute(
                        info = annotation.info(),
                        declareClass = it.declaringClass.kotlin,
                        method = it.kotlinFunction!!
                )
            }.also { attrs ->
                val annos = attrs.map { it.info.type }
                if (annos.toSet().size < attrs.size)
                    throw IllegalArgumentException("duplicate MQTransactionParticipant type: $annos")

                val invalidQueue = attrs.map { it.info.queue }.toSet().minus(config.queues.keys)
                if (invalidQueue.isNotEmpty())
                    throw IllegalArgumentException("Queue config for MQTransactionParticipant not found: ${invalidQueue.joinToString(", ")}")
            }

    fun getSourceQueueHandlers() = sourceQueueHandlers.toList()
            .filter { m -> m.kotlinFunction != null }
            .map { method ->
                val methodFullName = "${method.declaringClass.simpleName}.${method.name}"
//                source 不检查是否Transactional注解
//                if (method.getAnnotationsByType(Transactional::class.java).isEmpty())
//                    throw IllegalArgumentException("MQTransactionSource method need annotated with Transactional")
                val annotations =  method.getAnnotationsByType(MQTransactionSource::class.java)
                annotations.map {
                    if(it.queue.isBlank()){
                        throw IllegalArgumentException("MQTransactionSource missed queue property ( method: $methodFullName ) .")
                    }
                    if(it.type.isBlank()){
                        throw IllegalArgumentException("MQTransactionSource missed type property ( method: $methodFullName ) .")
                    }
                    TransactionSourceAttribute(
                            annotation = it.info(),
                            declareClass = method.declaringClass.kotlin,
                            method = method.kotlinFunction!!
                    )
                }.also { attrs ->
                    val annos = attrs.map { it.annotation.type }
                    if (annos.toSet().size < attrs.size)
                        throw IllegalArgumentException("Duplicate MQTransactionSource type: $annos, method: $methodFullName .")
                }
            }.flatten().also {
                attr->
                val invalidQueue = attr.map { it.annotation.queue }.toSet().minus(config.queues.keys)
                if (invalidQueue.isNotEmpty())
                    throw IllegalArgumentException("Queue config for MQTransactionSource not found: ${invalidQueue.joinToString(", ")}")
            }
}