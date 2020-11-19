package com.labijie.infra.mqts.ack

import java.net.URI

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */

data class AckRequestContext(
        val ackAddress: URI,
        val transactionId: Long,
        val transactionType: String,
        val queue: String,
        var transactionStates: Map<String, String> = mapOf())


open class AckRequest(
        var transactionId: Long = 0,
        var transactionType: String = "",
        var transactionStates: MutableMap<String, String> = mutableMapOf())

