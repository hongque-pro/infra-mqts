package com.labijie.infra.mqts

open class MQTransactionException(message: String? = null, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true)
    : RuntimeException(message, cause, enableSuppression, writableStackTrace) {

    constructor(message:String) : this(message, null, false, true)
}