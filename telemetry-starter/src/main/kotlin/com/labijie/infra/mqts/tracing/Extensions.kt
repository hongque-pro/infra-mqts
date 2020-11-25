package com.labijie.infra.mqts.tracing

import com.labijie.infra.mqts.context.ITransactionCompletionContext
import com.labijie.infra.mqts.context.ITransactionContext
import com.labijie.infra.telemetry.tracing.ScopeAndSpan
import com.labijie.infra.telemetry.tracing.propagation.MapGetter
import com.labijie.infra.telemetry.tracing.span
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.context.Context

private const val CONTEXT_KEY = "infra_telemetry_context"

const val SPAN_TAG_TRANSACTION_ID = "mqts-tran-id"

val ITransactionContext.telemetryContext: Context
    get() {
        synchronized(this) {
            return states.getOrPut(CONTEXT_KEY, {
                var context = Context.current()
                if (context.span == null) {
                    val propagator = OpenTelemetry.getGlobalPropagators().textMapPropagator
                    var remotingContext = propagator.extract(context, transaction.states, MapGetter.INSTANCE)

                    val parentTransaction = parentTransaction
                    if (remotingContext.span == null && parentTransaction != null) {
                        remotingContext = propagator.extract(context, parentTransaction.states, MapGetter.INSTANCE)
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
    states[CONTEXT_KEY] = this.telemetryContext.with(span)
}

fun ITransactionContext.scopeWithSpan(span: Span, closeCallback: (()-> Unit)?): ScopeAndSpan {
    val ctx = this.telemetryContext.with(span)
    val s = ctx.makeCurrent()
    return ScopeAndSpan(s, span, closeCallback)
}

internal fun Span.addCompletionStatus(eventName: String, context: ITransactionCompletionContext){
    this.addEvent(if (context.idempotent) "$eventName: idempotent" else "$eventName: done")
    if(context.hasError){
        this.setStatus(StatusCode.ERROR)
        this.recordException(context.exception!!)
    }else{
        this.setStatus(StatusCode.OK)
    }
}


