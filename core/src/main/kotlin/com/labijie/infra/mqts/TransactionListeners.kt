package com.labijie.infra.mqts

import com.labijie.infra.mqts.abstractions.ITransactionListener
import com.labijie.infra.mqts.context.ITransactionContext
import com.labijie.infra.mqts.context.TransactionContext
import com.labijie.infra.utils.throwIfNecessary

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-29
 */
class TransactionListeners : ArrayList<ITransactionListener>() {
    fun onTransactionCompleted(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionCompleted(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen complete failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun onTransactionExpired(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionExpired(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen expired failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun beforeTransactionRetryStart(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionRetryStarting(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen retry starting failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun onTransactionRetryStarted(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionRetryStarted(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen retry started failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun beforeTransactionStart(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionStarting(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen starting failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun onTransactionPrepared(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionPrepared(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen prepared failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun onTransactionStarted(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionStarted(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen started failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }


    fun onTransactionExecuting(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionExecuting(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen executing failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun onTransactionExecuted(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionExecuted(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen executed failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

//    fun onTransactionIdempotence(scopeContext: TransactionContext) {
//        this.forEach {
//            try {
//                it.onTransactionIdempotent(scopeContext)
//            } catch (ex: Throwable) {
//                MQTransactionManager.logger.warn("MQ transaction listen idempotence failed.", ex)
//                ex.throwIfNecessary()
//            }
//        }
//    }

    fun onTransactionAckReceived(context: ITransactionContext) {
        this.forEach {
            try {
                it.onTransactionAckReceived(context)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen ack received failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }


    fun onTransactionAckRequested(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionAckRequested(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listen ack failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }

    fun onTransactionAckRequesting(scopeContext: TransactionContext) {
        this.forEach {
            try {
                it.onTransactionAckRequesting(scopeContext)
            } catch (ex: Throwable) {
                MQTransactionManager.logger.warn("MQ transaction listening before ack failed.", ex)
                ex.throwIfNecessary()
            }
        }
    }
}