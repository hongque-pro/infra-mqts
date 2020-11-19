package com.labijie.infra.mqts

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-10
 *
 *
 */

/**
 * 用于携带 MQ 数据
 */
interface IMQTransactionPayload {
    fun extract(): Map<String, String>
}