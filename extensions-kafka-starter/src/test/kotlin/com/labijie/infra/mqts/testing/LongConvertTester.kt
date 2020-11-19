package com.labijie.infra.mqts.testing

import com.labijie.infra.mqts.kafka.redo.LongConverter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-16
 */
class LongConvertTester {

    @RepeatedTest(10)
    fun randomTest(){
        val longValue = Random.nextLong()
        val  buffer = LongConverter.writeLong(longValue)
        val longValue2 = LongConverter.readLong(buffer)

        Assertions.assertEquals(longValue, longValue2)
    }
}