package com.labijie.infra.mqts.spring.interceptor

import com.sun.xml.internal.fastinfoset.util.StringArray

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-07
 */
internal class MQTransactionSelectionHolder private constructor() {

    companion object {
        val DEFAULT: MQTransactionSelectionHolder = MQTransactionSelectionHolder()
    }

    private val transaction:ThreadLocal<Array<out String>> = ThreadLocal()

    var currentTransactions : Array<out String>
        get() = this.transaction.get() ?: arrayOf()
        set(value) {
            if(value.isEmpty()){
                this.transaction.remove()
            }else{
                this.transaction.set(value)
            }
        }
}