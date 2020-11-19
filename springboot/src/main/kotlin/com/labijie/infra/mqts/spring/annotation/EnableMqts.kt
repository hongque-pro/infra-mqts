package com.labijie.infra.mqts.spring.annotation

import com.labijie.infra.mqts.spring.configuration.MqtsRunnerSelector
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.EnableTransactionManagement


@EnableTransactionManagement
@Import(MqtsRunnerSelector::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnableMqts(val autoStartService: Boolean = true) {

}