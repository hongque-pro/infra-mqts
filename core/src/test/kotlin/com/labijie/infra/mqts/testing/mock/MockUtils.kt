package com.labijie.infra.mqts.testing.mock

import com.labijie.infra.mqts.MQTransactionManager
import com.labijie.infra.mqts.abstractions.IAckClient
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.abstractions.IIdempotence
import com.labijie.infra.mqts.abstractions.ITransactionRepository
import org.mockito.Mockito

class MockUtils {
    companion object {
        fun mqTransactionManager(): MQTransactionManager {
            val idempotence = Mockito.mock(IIdempotence::class.java)
            val transactionRepository = Mockito.mock(ITransactionRepository::class.java)
            val ackClient = Mockito.mock(IAckClient::class.java)
            val ackServer = Mockito.mock(IAckServer::class.java)
            //Mockito.`when`(idempotence.ensureIdempotence(Mockito.any(MQTransaction::class.java)))
            val mananger = MQTransactionManager(idempotence, transactionRepository)
            return mananger
        }
    }
}