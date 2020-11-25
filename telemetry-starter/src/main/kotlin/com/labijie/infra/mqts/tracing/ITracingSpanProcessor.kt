package com.labijie.infra.mqts.tracing

import io.opentelemetry.api.trace.SpanBuilder


interface ITracingSpanProcessor {
    fun processSpan(builder: SpanBuilder)
}