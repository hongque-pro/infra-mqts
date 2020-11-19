package com.labijie.infra.mqts.discovery.web

import com.labijie.infra.mqts.TransactionResult
import com.labijie.infra.mqts.ack.AckRequest

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
class AckModel(
        var result:TransactionResult = TransactionResult.Committed,
        var queue:String = "",
        transactionId: Long = 0,
        transactionType: String = "",
        transactionStates: Map<String, String> = mapOf()) : AckRequest(transactionId, transactionType, transactionStates.toMutableMap()) {

}