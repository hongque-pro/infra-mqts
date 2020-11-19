package com.labijie.infra.mqts.spring.condition

import com.labijie.infra.mqts.spring.configuration.MqtsRunnerSelector
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

internal class MqtsCondition : SpringBootCondition() {
    override fun getMatchOutcome(context: ConditionContext?, metadata: AnnotatedTypeMetadata?): ConditionOutcome {
        return if(MqtsRunnerSelector.isImported) ConditionOutcome.match() else ConditionOutcome.noMatch("EnableMQTransaction was not annotated.")
    }
}