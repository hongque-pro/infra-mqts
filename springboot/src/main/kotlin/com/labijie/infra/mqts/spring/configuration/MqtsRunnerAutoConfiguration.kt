package com.labijie.infra.mqts.spring.configuration

import com.labijie.infra.mqts.spring.MqtsCommandLineRunner
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 */

@Configuration(proxyBeanMethods = false)
class MqtsRunnerAutoConfiguration {
    @Bean
    fun mqTransactionRunner(): CommandLineRunner {
        return MqtsCommandLineRunner()
    }
}