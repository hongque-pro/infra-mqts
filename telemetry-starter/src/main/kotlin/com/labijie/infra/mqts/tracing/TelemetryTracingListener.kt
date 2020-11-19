package com.labijie.infra.mqts.tracing

import com.labijie.infra.mqts.abstractions.ITransactionListener
import com.labijie.infra.mqts.context.ITransactionCompletionContext
import com.labijie.infra.mqts.context.ITransactionContext
import com.labijie.infra.mqts.context.ITransactionInitializationContext
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.telemetry.tracing.ScopeAndSpan
import com.labijie.infra.telemetry.tracing.injectSpan
import com.labijie.infra.telemetry.tracing.span
import com.labijie.infra.utils.ifNullOrBlank
import com.labijie.infra.utils.nowString
import com.labijie.infra.utils.throwIfNecessary
import io.opentelemetry.common.AttributeConsumer
import io.opentelemetry.common.AttributeKey
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.trace.Span
import io.opentelemetry.trace.Tracer
import org.slf4j.LoggerFactory
import java.lang.StringBuilder


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-28
 */
class TelemetryTracingListener(private val applicationName: String,
                               private val tracer: Tracer,
                               networkConfig: NetworkConfig) : ITransactionListener {

    companion object {

        private const val CONTEXT_ROOT_SCOPE_KEY = "infra_telemetry_root_scope"
        private const val CONTEXT_EXEC_SCOPE_KEY = "infra_telemetry_exec_scope"
        private const val CONTEXT_ACK_SCOPE_KEY = "infra_telemetry_ack_scope"
        private const val CONTEXT_RETRY_SCOPE_KEY = "infra_telemetry_retry_scope"
        private const val CONTEXT_COMMIT_SCOPE_KEY = "infra_telemetry_commit_scope"

        internal val logger = LoggerFactory.getLogger(TelemetryTracingListener::class.java)!!


        private fun writeLog(methodName: String, scope: ScopeAndSpan) {
            if (logger.isDebugEnabled) {
                val dataSpan = scope.span as? ReadableSpan
                val spanData = dataSpan?.toSpanData()
                if(spanData != null) {
                    val builder = StringBuilder().apply {
                        this.appendLine("Debug trace info:")
                        this.appendLine("--==$methodName==--")
                        this.appendLine("trace id: ${spanData.traceId}")
                        this.appendLine("span id: ${spanData.spanId}")
                        this.appendLine("parent span id: ${spanData.parentSpanId.ifNullOrBlank { "<null>" }}")
                        this.appendLine("span name: ${spanData.name}")
                        this.appendLine("kind: ${spanData.kind}")
                        this.appendLine("status: ${spanData.status.canonicalCode}")
                        this.appendLine("attributes: ")
                        val b = this
                        spanData.attributes.forEach(object : AttributeConsumer{
                            override fun <T : Any?> consume(key: AttributeKey<T>, value: T) {
                                b.appendLine("  ${key.key}=${value}")
                            }
                        })
                    }
                    logger.debug(builder.toString())
                }else {
                    logger.debug("${System.lineSeparator()}Trace [$methodName] (thread:${Thread.currentThread().id}): ${scope.span}")
                }
            }
        }
    }


    private fun getSourceSpanName(action: String) = "source: $action"
    private fun getParticipantSpanName(action: String) = "participant: $action"
    private val ipAddress = networkConfig.getIPAddress()

    private fun ITransactionContext.getOrNewRootScope(customizer: (Span.Builder.() -> Unit)? = null): ScopeAndSpan =
            getOrNewScope("mqts:${this.transaction.transactionType}", CONTEXT_ROOT_SCOPE_KEY, customizer)

    private fun ITransactionContext.newScope(spanName: String, scopeKey: String, customizer: (Span.Builder.() -> Unit)? = null): ScopeAndSpan {
        val span = tracer.spanBuilder(spanName)
                .init()
                .also {
                    if(this.telemetryContext.span != null) {
                        it.setParent(this.telemetryContext)
                    }
                    customizer?.invoke(it)
                }
                .startSpan().also {
                   this.withSpan(it)
                }

        val s =this.scopeWithSpan(span) {
            this.states.remove(scopeKey)
        }

        return s.also {
            if (!scopeKey.isBlank()) {
                this.states[scopeKey] = it
            }
        }
    }

    private fun ITransactionContext.getOrNewScope(spanName: String, scopeKey: String, customizer: (Span.Builder.() -> Unit)? = null): ScopeAndSpan {
        val scope = this.states.getOrElse(scopeKey) {
            this.newScope(spanName, scopeKey, customizer)
        }
        return scope as ScopeAndSpan
    }

    private val ITransactionContext.rootScope: ScopeAndSpan?
        get() = this.states.getOrDefault(CONTEXT_ROOT_SCOPE_KEY, null) as? ScopeAndSpan

    private fun Span.Builder.init(): Span.Builder {
        this.setAttribute("event-time", nowString())
        this.setAttribute("application", applicationName)
        this.setAttribute("ip-address", ipAddress)
        return this
    }


    override fun onTransactionPrepared(transactionScope: ITransactionInitializationContext) {
        val scope = transactionScope.getOrNewRootScope {
            this.setAttribute(SPAN_TAG_TRANSACTION_ID, transactionScope.transaction.transactionId)
            try {
                (transactionScope.methodReturnValue as? ITracingSpanProcessor)?.processSpan(this)
            } catch (t: Throwable) {
                logger.error("ERROR on processSpan", t)
                t.throwIfNecessary()
            }
        }
        tracer.injectSpan(transactionScope.transaction.states, transactionScope.telemetryContext)
        writeLog(TelemetryTracingListener::onTransactionPrepared.name, scope = scope)
    }

    override fun onTransactionStarting(transactionScope: ITransactionContext) {
        transactionScope.rootScope?.apply {
            this.span.addEvent("starting")
            writeLog(TelemetryTracingListener::onTransactionStarting.name, this)
        }
    }

    override fun onTransactionStarted(transactionScope: ITransactionCompletionContext) {
        transactionScope.rootScope?.use {
            it.span.addCompletionStatus("started", transactionScope)
            writeLog(TelemetryTracingListener::onTransactionStarted.name, it)
        }
    }


    override fun onTransactionExpired(transactionScope: ITransactionContext) {
        var isNew = false
        val s = transactionScope.getOrNewScope("execution", CONTEXT_EXEC_SCOPE_KEY) {
            isNew = true
        }.apply {
            writeLog(TelemetryTracingListener::onTransactionExpired.name, this)
            if (isNew) {
                this.close()
            }
        }
        writeLog(TelemetryTracingListener::onTransactionExpired.name, scope = s)
    }

    override fun onTransactionAckReceived(transactionScope: ITransactionContext) {
        val eventName = getSourceSpanName("commit")
        transactionScope.getOrNewScope(eventName, CONTEXT_COMMIT_SCOPE_KEY).apply {
            writeLog(TelemetryTracingListener::onTransactionAckReceived.name, this)
        }
    }

    override fun onTransactionCompleted(transactionScope: ITransactionCompletionContext) {
        val spanName = getSourceSpanName("commit")
        val statusEvent = if (transactionScope.idempotent) "idempotent" else "committed"
        transactionScope.getOrNewScope(spanName, CONTEXT_COMMIT_SCOPE_KEY).use {
            it.span.addCompletionStatus(statusEvent, context = transactionScope)
            writeLog(TelemetryTracingListener::onTransactionCompleted.name, it)
        }
    }

    override fun onTransactionExecuting(transactionScope: ITransactionContext) {
        val eventName = getParticipantSpanName("execution")
        transactionScope.getOrNewScope(eventName, CONTEXT_EXEC_SCOPE_KEY).apply {
            tracer.injectSpan(transactionScope.transaction.states, transactionScope.telemetryContext)
            writeLog(TelemetryTracingListener::onTransactionExecuting.name, this)
        }
    }

    override fun onTransactionExecuted(transactionScope: ITransactionCompletionContext) {
        val eventName = getParticipantSpanName("execution")

        transactionScope.getOrNewScope(eventName, CONTEXT_EXEC_SCOPE_KEY).use {
            it.span.addCompletionStatus("done", transactionScope)
            writeLog(TelemetryTracingListener::onTransactionExecuted.name, it)
        }
    }

//    override fun onTransactionIdempotent(transactionScope: ITransactionContext) {
//        transactionScope.newScope("transaction-idempotent").apply {
//            writeLog(TrackingListener::onTransactionIdempotent.name, this)
//            this.close()
//        }
//    }

    override fun onTransactionAckRequesting(transactionScope: ITransactionContext) {
        val eventName = getParticipantSpanName("ack-requesting")
        transactionScope.getOrNewScope(eventName, CONTEXT_ACK_SCOPE_KEY).apply {
            writeLog(TelemetryTracingListener::onTransactionAckRequesting.name, this)
            tracer.injectSpan(transactionScope.transaction.states, transactionScope.telemetryContext)
        }
    }

    override fun onTransactionAckRequested(transactionScope: ITransactionCompletionContext) {
        val eventName = getParticipantSpanName("ack-requesting")
        transactionScope.getOrNewScope(eventName, CONTEXT_ACK_SCOPE_KEY).use {
            it.span.addCompletionStatus("request", transactionScope)
            writeLog(TelemetryTracingListener::onTransactionAckRequested.name, it)
        }
    }


    override fun onTransactionRetryStarting(transactionScope: ITransactionContext) {
        val spanName = getSourceSpanName("mqts:${transactionScope.transaction.transactionType}:retry")
        transactionScope.getOrNewScope(spanName, CONTEXT_RETRY_SCOPE_KEY).apply {
            writeLog(TelemetryTracingListener::onTransactionRetryStarting.name, this)
        }
    }

    override fun onTransactionRetryStarted(transactionScope: ITransactionCompletionContext) {
        val spanName = getSourceSpanName("mqts:${transactionScope.transaction.transactionType}:retry")
        transactionScope.getOrNewScope(spanName, CONTEXT_RETRY_SCOPE_KEY).use {
            writeLog(TelemetryTracingListener::onTransactionRetryStarted.name, it)
        }
    }
}