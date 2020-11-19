package com.labijie.infra.mqts

import io.netty.util.Timeout
import com.labijie.infra.mqts.abstractions.ITransactionQueue
import com.labijie.infra.mqts.context.TransactionContext
import java.util.concurrent.atomic.AtomicInteger

class TransactionProducingContext(
        val mq: ITransactionQueue,
        val transaction: MQTransaction,
        val source: TransactionSourceAttribute,
        var isNew: Boolean,
        rootContextStates: Map<String, Any>) {

    val rootStates: Map<String, Any> = rootContextStates.map {
        it.key to it.value
    }.toMap()

    private val retryCount: AtomicInteger = AtomicInteger()

    @Volatile
    var isCancelled: Boolean = false
        private set

    @Volatile
    var isRunning: Boolean = false
        private set

    var timeout: Timeout? = null

    fun execute(scope: TransactionContext) {
        if (!isRunning) {
            isRunning = true
            try {
                mq.send(source.annotation.queue, scope.transaction)
            } finally {
                isRunning = false
            }
        }
    }

    fun nextRetrySeconds(): Long {
        val interval = if (source.annotation.retryIntervalSeconds.isEmpty()) MQTransactionManager.DEFAULT_RETRY_TICKS else source.annotation.retryIntervalSeconds
        return getNextRetrySeconds(getRetryCount(), interval).toLong()
    }

    fun cancel() {
        isCancelled = true
        this.timeout?.cancel()
        this.timeout = null
    }

    fun addRetryCount() = this.retryCount.incrementAndGet()

    fun getRetryCount() = this.retryCount.get()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionProducingContext

        return (transaction.transactionId == other.transaction.transactionId)
    }

    override fun hashCode(): Int {
        return transaction.transactionId.hashCode()
    }

}