package com.labijie.infra.mqts.tracing

import io.opentelemetry.trace.Span

interface ITracingSpanProcessor {
    fun processSpan(builder: Span.Builder)
}