package com.labijie.infra.mqts.testing

import com.labijie.infra.utils.printStackToString
import org.junit.jupiter.api.Test

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-08-16
 */
class UtilsTester {

    @Test
    fun printStackTest(){
        try {
            throw IllegalArgumentException("ttttt")
        }
        catch (ex:Throwable){
            println(ex.printStackToString())
        }
    }
}