package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.ISourceInfo
import com.labijie.infra.mqts.MQTransaction

interface ITransactionRepository {
    fun getAvailable(): Array<out MQTransaction>
    fun deleteByTransactionId(transactionId: Long): Boolean
    fun expireByTransactionId(transactionId: Long): Boolean
    fun getById(transactionId: Long): MQTransaction?
    fun isTransactionExisted(transactionId: Long): Boolean
    fun recoverExpiredTransaction(transactionId: Long, timeoutSeconds:Int): MQTransaction?
    fun clearExpired()
    fun save(mqTransaction: MQTransaction, attribute: ISourceInfo)
}