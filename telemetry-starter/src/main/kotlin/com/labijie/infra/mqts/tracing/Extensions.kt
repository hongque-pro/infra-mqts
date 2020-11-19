package com.labijie.infra.mqts.tracing

import com.labijie.infra.mqts.context.ITransactionCompletionContext
import com.labijie.infra.mqts.context.ITransactionContext
import com.labijie.infra.telemetry.tracing.ScopeAndSpan
import com.labijie.infra.telemetry.tracing.propagation.MapGetter
import com.labijie.infra.telemetry.tracing.span
import io.grpc.Context
import io.opentelemetry.OpenTelemetry
import io.opentelemetry.context.ContextUtils
import io.opentelemetry.trace.Span
import io.opentelemetry.trace.StatusCanonicalCode
import io.opentelemetry.trace.TracingContextUtils

private const val CONTEXT_KEY = "infra_telemetry_context"

const val SPAN_TAG_TRANSACTION_ID = "mqts-tran-id"

val ITransactionContext.telemetryContext: Context
    get() {
        synchronized(this) {
            return states.getOrPut(CONTEXT_KEY, {
                var context = Context.current()
                if (context.span == null) {
                    val propagator = OpenTelemetry.getPropagators().textMapPropagator
                    var remotingContext = propagator.extract(context, transaction.states, MapGetter)

                    val parentTransaction = parentTransaction
                    if (remotingContext.span == null && parentTransaction != null) {
                        remotingContext = propagator.extract(context, parentTransaction.states, MapGetter)
                    }
                    if(remotingContext.span != null) {
                        context = remotingContext
                    }
                }
                context
            }) as Context
        }
    }

fun ITransactionContext.withSpan(span: Span) {
    states[CONTEXT_KEY] = TracingContextUtils.withSpan(span, this.telemetryContext)
}

fun ITransactionContext.scopeWithSpan(span: Span, closeCallback: (()-> Unit)?): ScopeAndSpan {
    val ctx = TracingContextUtils.withSpan(span, this.telemetryContext)
    val s = ContextUtils.withScopedContext(ctx)
    return ScopeAndSpan(s, span, closeCallback)
}

internal fun Span.addCompletionStatus(eventName: String, context: ITransactionCompletionContext){
    this.addEvent(if (context.idempotent) "$eventName: idempotent" else "$eventName: done")
    if(context.hasError){
        this.setStatus(StatusCanonicalCode.ERROR, context.exception!!.stackTraceToString())
    }else{
        this.setStatus(StatusCanonicalCode.OK)
    }
}


