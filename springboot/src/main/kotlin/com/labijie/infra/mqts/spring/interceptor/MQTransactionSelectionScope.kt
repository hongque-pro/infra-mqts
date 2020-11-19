package com.labijie.infra.mqts.spring.interceptor

import com.sun.xml.internal.fastinfoset.util.StringArray

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-07
 */
class MQTransactionSelectionScope private constructor(vararg transactionTypes:String): AutoCloseable {

    init {
        MQTransactionSelectionHolder.DEFAULT.currentTransactions = transactionTypes
    }

    override fun close() {
        MQTransactionSelectionHolder.DEFAULT.currentTransactions = arrayOf()
    }

    companion object {
        fun withTransactionTypes(vararg transactionTypes:String) : MQTransactionSelectionScope {
            return MQTransactionSelectionScope(*transactionTypes)
        }
    }
}