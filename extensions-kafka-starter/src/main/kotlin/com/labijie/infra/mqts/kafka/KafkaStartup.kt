package com.labijie.infra.mqts.kafka

import com.labijie.infra.mqts.spring.IMQTransactionStartup
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import java.util.*

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
class KafkaStartup(private val consumers: KafkaConsumers) : IMQTransactionStartup {

    companion object{
        private val logger by lazy { LoggerFactory.getLogger(KafkaStartup::class.java) }
    }

    override fun getOrder(): Int  = Int.MAX_VALUE - 50

    override fun start(applicationContext: ApplicationContext) {

        val handlers = applicationContext.getBeansOfType(IKafkaRecordHandler::class.java)
        val sortedSet = sortedSetOf(Comparator { o1, o2 ->
            if (o1 == null || o2 == null) {
                throw IllegalArgumentException("Comparator element cant not be null.")
            }
            val r = o1.ordered.compareTo(o2.ordered)
            if (r == 0) o1.hashCode().compareTo(o2.hashCode()) else r
        }, *handlers.values.toTypedArray())

        consumers.start(sortedSet)

        logger.info("MQTS kafka component was started.")
    }
}