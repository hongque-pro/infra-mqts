package com.labijie.infra.mqts

class IdempotenceException(message: String?, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true) : MQTransactionException(message, cause, enableSuppression, writableStackTrace) {
}