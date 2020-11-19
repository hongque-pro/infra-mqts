package com.labijie.infra.mqts.discovery

import com.labijie.infra.mqts.MQTransactionException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
class MQTransactionDiscoveryException(message: String?, cause: Throwable? = null, enableSuppression: Boolean = false, writableStackTrace: Boolean = true) : MQTransactionException(message, cause, enableSuppression, writableStackTrace) {
}