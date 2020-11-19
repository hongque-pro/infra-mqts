package com.labijie.infra.mqts.spring.interceptor

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.context.TransactionContext
import org.springframework.transaction.support.TransactionSynchronizationAdapter


class MQTransactionSourceSyncAdapter(
        private val ackServer: IAckServer,
        private val mqTransactionManager: MQTransactionManager,
        private val transactionScopeContext: TransactionContext)
    : TransactionSynchronizationAdapter()  {

    override fun afterCommit() {
        this.mqTransactionManager.beginTransaction(ackServer, transactionScopeContext)
    }

//    override fun afterCompletion(status: Int) {
//        val session = TransactionSynchronizationManager.getResource(this.sessionFactory) as MQSession
//        when(status){
//            STATUS_COMMITTED -> session.commitTransaction()
//
//        }
//    }

}