package com.labijie.infra.mqts.spring.interceptor

import com.labijie.infra.mqts.*
import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.Method

internal class MQTransactionAnnotationParser {
    companion object {
        fun parseSource(element: Method): List<TransactionSourceAttribute> {
          return element.getAnnotationsByType(MQTransactionSource::class.java).map {
            TransactionSourceAttribute(
                annotation = it.info(),
                declareClass = element.declaringClass.kotlin,
                method = element::invoke
            )
          }
        }

        fun parseParticipant(element: Method): TransactionParticipantAttribute? {
            val participant = AnnotatedElementUtils.findMergedAnnotation(element, MQTransactionParticipant::class.java)
            if(participant != null) {
                return TransactionParticipantAttribute(participant.info(), element.declaringClass.kotlin, element::invoke)
            }
            return null
        }
    }
}