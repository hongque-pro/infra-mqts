package com.labijie.infra.mqts.testing

import com.labijie.infra.mqts.getNextRetrySeconds
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-15
 */
class TransactionSourceAttributeTester {
    @Test
    fun nextTimeTest(){
        val tickers = intArrayOf(5, 5, 10, 30, 60)

        var value = getNextRetrySeconds(0, tickers)
        Assertions.assertEquals(5, value)

        value = getNextRetrySeconds(1, tickers)
        Assertions.assertEquals(5, value)

        value = getNextRetrySeconds(2, tickers)
        Assertions.assertEquals(10, value)

        value = getNextRetrySeconds(3, tickers)
        Assertions.assertEquals(30, value)

        value = getNextRetrySeconds(4, tickers)
        Assertions.assertEquals(60, value)

        value = getNextRetrySeconds(5, tickers)
        Assertions.assertEquals(60, value)
    }
}