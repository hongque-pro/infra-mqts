package com.labijie.infra.mqts

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-16
 */
class RecoverableException(message: String, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true)
    : MQTransactionException(message, cause, enableSuppression, writableStackTrace) {
}