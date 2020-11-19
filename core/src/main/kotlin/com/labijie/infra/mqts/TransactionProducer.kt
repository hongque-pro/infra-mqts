package com.labijie.infra.mqts

import com.labijie.infra.mqts.context.TransactionContext
import com.labijie.infra.utils.nowString
import com.labijie.infra.utils.throwIfNecessary
import io.netty.util.Timeout
import io.netty.util.Timer
import io.netty.util.TimerTask
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-29
 */
class TransactionProducer(
        private val mqTransactionManager: MQTransactionManager,
        private val retryTime: Timer,
        private val context: TransactionProducingContext,
        private val listeners: TransactionListeners) : TimerTask {

    companion object {
        private val logger = LoggerFactory.getLogger(TransactionProducer::class.java)
    }

    @Deprecated(message = "run use for internal", replaceWith = ReplaceWith("execute()"))
    override fun run(timeout: Timeout?) {
        execute()
    }

    fun execute(): Boolean {
        if (!context.isCancelled) {
            //可能 ACK 和 source 不在同一个服务器，多查询一次，防止多余的重试
            if (mqTransactionManager.isTransactionExisted(context.transaction.transactionId)) {
                if (!context.transaction.isExpired(context.source)) {
                    val scopeContext = TransactionContext(context.transaction.copy()).also { ctx ->
                        this.context.rootStates.forEach { (k, v) -> ctx.states[k] = v }
                    }

                    context.addRetryCount()
                    if (context.isNew) listeners.beforeTransactionStart(scopeContext) else listeners.beforeTransactionRetryStart(scopeContext)
                    if (!context.isNew) {
                        if (logger.isDebugEnabled) {
                            logger.debug("Try ${context.getRetryCount()} time on a transaction '${context.transaction.transactionId}' at ${nowString()}")
                        }
                    }
                    try {
                        context.execute(scopeContext)
                    } catch (ex: Throwable) {
                        scopeContext.exception = ex
                        MQTransactionManager.logger.error("An error occurred while sending the message to transaction mq.", ex)
                        ex.throwIfNecessary()
                    } finally {
                        val isNew = context.isNew
                        if (context.isNew) {
                            context.isNew = false
                        }
                        if (!context.isCancelled) {
                            context.timeout = retryTime.newTimeout(this, context.nextRetrySeconds(), TimeUnit.SECONDS)
                        }
                        if (isNew) listeners.onTransactionStarted(scopeContext) else listeners.onTransactionRetryStarted(scopeContext)
                    }
                    return true

                } else {
                    mqTransactionManager.expireTransaction(TransactionContext(context.transaction.copy()))
                }
            } else {
                mqTransactionManager.removeTransactionRetryTask(context.transaction)
            }
        }
        return false
    }
}