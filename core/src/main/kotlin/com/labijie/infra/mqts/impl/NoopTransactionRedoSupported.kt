package com.labijie.infra.mqts.impl

import com.labijie.infra.mqts.TransactionSourceAttribute
import com.labijie.infra.mqts.abstractions.ITransactionRedoSupported
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-26
 */
class NoopTransactionRedoSupported() : ITransactionRedoSupported {
    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(NoopTransactionRedoSupported::class.java) }
    }

    @Throws(UnsupportedOperationException::class)
    override fun start(transactionSources: Iterable<TransactionSourceAttribute>) {
        logger.warn("MQTS redo supported was not found, so transaction can not be redo.")
    }
}