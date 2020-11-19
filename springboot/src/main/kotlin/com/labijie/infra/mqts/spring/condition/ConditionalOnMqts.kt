package com.labijie.infra.mqts.spring.condition

import org.springframework.context.annotation.Conditional


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(MqtsCondition::class)
annotation class ConditionalOnMqts