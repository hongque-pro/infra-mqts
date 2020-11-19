package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.IdempotenceException
import com.labijie.infra.mqts.MQTransaction

/*
幂等性接口
 */
interface IIdempotence {
    /**
     * 通过抛出异常来去确定一个幂等操作
     */
    @Throws(IdempotenceException::class)
    fun ensureIdempotence(transaction: MQTransaction)
}