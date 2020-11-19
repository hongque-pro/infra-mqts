package com.labijie.infra.mqts

/**
 *
 * @author lishiwen
 * @date 18-8-8
 * @since JDK1.8
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class MQTransactionSources(
    vararg val value: MQTransactionSource
)