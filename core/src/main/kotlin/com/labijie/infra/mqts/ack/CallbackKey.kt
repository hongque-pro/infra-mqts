package com.labijie.infra.mqts.ack

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
data class CallbackKey(val queue:String, val transactionType:String) {
}