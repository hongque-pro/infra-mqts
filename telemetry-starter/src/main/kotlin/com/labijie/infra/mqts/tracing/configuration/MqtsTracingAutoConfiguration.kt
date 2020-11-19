package com.labijie.infra.mqts.tracing.configuration

import com.labijie.infra.mqts.spring.condition.ConditionalOnMqts
import com.labijie.infra.mqts.tracing.TelemetryTracingListener
import com.labijie.infra.spring.configuration.CommonsAutoConfiguration
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.spring.configuration.getApplicationName
import com.labijie.infra.telemetry.configuration.TelemetryAutoConfiguration
import com.labijie.infra.telemetry.tracing.TracingManager
import io.opentelemetry.trace.Tracer
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-28
 */
@AutoConfigureAfter(TelemetryAutoConfiguration::class, Environment::class, CommonsAutoConfiguration ::class)
@ConditionalOnMqts
@Configuration
class MqtsTracingAutoConfiguration {
    @Bean
    @ConditionalOnBean(TracingManager::class)
    @ConditionalOnProperty(
            name = ["infra.mqts.tracing.enabled"],
            havingValue = "true",
            matchIfMissing = true
    )
    fun telemetryTracingListener(tracer: Tracer, environment: Environment, networkConfig: NetworkConfig): TelemetryTracingListener {
        return TelemetryTracingListener(environment.getApplicationName(), tracer, networkConfig)
    }
}