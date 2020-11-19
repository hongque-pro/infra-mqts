package com.labijie.infra.mqts.kafka.redo

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-16
 */
object LongConverter {
    fun readLong(buf: ByteArray): Long {
        return (buf[0].toInt() and 0xff).toLong() shl 56 or
                ((buf[1].toInt() and 0xff).toLong() shl 48) or
                ((buf[2].toInt() and 0xff).toLong() shl 40) or
                ((buf[3].toInt() and 0xff).toLong() shl 32) or
                ((buf[4].toInt() and 0xff).toLong() shl 24) or
                ((buf[5].toInt() and 0xff).toLong() shl 16) or
                ((buf[6].toInt() and 0xff).toLong() shl 8) or
                (buf[7].toInt() and 0xff).toLong()

    }

    fun writeLong(value: Long): ByteArray {
        val longOut = ByteArray(8)
        longOut[0] = (0xff.toLong() and (value shr 56)).toByte()
        longOut[1] = (0xff.toLong() and (value shr 48)).toByte()
        longOut[2] = (0xff.toLong() and (value shr 40)).toByte()
        longOut[3] = (0xff.toLong() and (value shr 32)).toByte()
        longOut[4] = (0xff.toLong() and (value shr 24)).toByte()
        longOut[5] = (0xff.toLong() and (value shr 16)).toByte()
        longOut[6] = (0xff.toLong() and (value shr 8)).toByte()
        longOut[7] = (0xff.toLong() and value).toByte()
        return longOut
    }
}