package com.labijie.infra.mqts.kafka

import com.labijie.infra.NamedThreadFactory
import com.labijie.infra.mqts.MQTransactionException
import com.labijie.infra.mqts.configuration.MQTransactionConfig
import com.labijie.infra.utils.throwIfNecessary
import org.apache.kafka.clients.consumer.*
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.LongDeserializer
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-19
 */
class KafkaConsumers(
        private val applicationName: String?,
        private val config: MQTransactionConfig,
        private val isDevelopment: Boolean = false,
        private val ackEnabled: Boolean = true,
        private val redoEnabled: Boolean = true) : AutoCloseable {

    @Volatile
    var isStarted: Boolean = false

    companion object {
        @JvmStatic
        val logger = LoggerFactory.getLogger(KafkaConsumers::class.java)!!
    }

    private val keyDeserializer = LongDeserializer()
    private val dataDeserializer = ByteArrayDeserializer()
    private val queueTopics: ConcurrentHashMap<String, MutableSet<String>> = ConcurrentHashMap()
    private val consumers: MutableSet<KafkaConsumer<Long, ByteArray>> = mutableSetOf()

    private fun closeConsumer(consumer: KafkaConsumer<Long, ByteArray>) {
        try {
            consumer.close()
        } catch (ex: KafkaException) {
            logger.warn("Close mqts kafka consumer fault.", ex)
        }
    }


    fun addTopic(queue: String, topic: String) {
        val topics = this.queueTopics.getOrPut(queue) {
            mutableSetOf()
        }
        topics.add(topic)
    }

    private fun createConsumer(queue: String, topics: Collection<String>, handlerType: IKafkaRecordHandler.HandlerType): KafkaConsumer<Long, ByteArray> {
        val conf = config.queues[queue]!!
        val properties = conf.loadKafkaConsumerConfig()
        val type = handlerType.toString().toLowerCase()
        val groupId = if (applicationName.isNullOrBlank()) "mqts-$type" else "$applicationName-for-$type"
        properties[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        properties.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 3 * topics.count())
        properties.putIfAbsent(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 90000)
        properties.putIfAbsent(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 30000)
        properties.putIfAbsent(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 4 * 1024 * 1024)
        properties.putIfAbsent(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                if (isDevelopment)
                    TimeUnit.SECONDS.toMillis(20).toInt() else
                    maxPollIntervalMills(topics))
        properties.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        return KafkaConsumer(properties, this.keyDeserializer, this.dataDeserializer)
    }

    private fun maxPollIntervalMills(topics: Collection<String>) =
            (TimeUnit.SECONDS.toMillis(45).toInt() * topics.count()).coerceAtLeast(TimeUnit.MINUTES.toMillis(2).toInt())

    @Synchronized
    override fun close() {
        if(isStarted) {
            isStarted = false
            val cs = this.consumers.toTypedArray()
            cs.forEach {
                this.closeConsumer(it)
            }
            this.consumers.clear()
            logger.info("MQTS participant was stopped .")
        }
    }

    fun start(handlers: Collection<IKafkaRecordHandler>) {
        if (!this.isStarted) {
            this.isStarted = true
            if (queueTopics.isNotEmpty()) {

                queueTopics.forEach { queue ->

                    val queueConfig = config.queues.getOrDefault(queue.key, null)
                    val pntPoolSize = queueConfig?.properties?.getOrDefault(Constants.PARTICIPANT_POOL_SIZE, (Runtime.getRuntime().availableProcessors() * 2).toString()) as? String
                    val ackPoolSize = queueConfig?.properties?.getOrDefault(Constants.ACK_POOL_SIZE, (Runtime.getRuntime().availableProcessors()).toString()) as? String

                    val pps = try {
                        pntPoolSize?.toInt()
                    } catch (ex: NumberFormatException) {
                        throw MQTransactionException("${Constants.PARTICIPANT_POOL_SIZE} config for MQTS must be number")
                    } ?: (Runtime.getRuntime().availableProcessors() * 2)
                    val aps = try {
                        ackPoolSize?.toInt()
                    } catch (ex: NumberFormatException) {
                        throw MQTransactionException("${Constants.ACK_POOL_SIZE} config for MQTS must be number")
                    } ?: (Runtime.getRuntime().availableProcessors())

                    val ackTopics = getAckTopics(queue)
                    val participantTopics = getParticipantTopics(queue)
                    val redoTopics = getAckRedoTopics(queue)

                    startConsumer("mqts-participant-kafka-thread", queue, pps, participantTopics, handlers.firstOrNull { it.type == IKafkaRecordHandler.HandlerType.Participant })
                    logger.info("MQTS subscribe for participant (${participantTopics.size}): ${participantTopics.joinToString(", ")}")

                    if (ackEnabled) {
                        startConsumer("mqts-ack-kafka-thread", queue, aps, ackTopics, handlers.firstOrNull { it.type == IKafkaRecordHandler.HandlerType.Ack })
                        logger.info("MQTS subscribe for ack (${ackTopics.size}): ${ackTopics.joinToString(", ")}")
                    }

                    if (redoEnabled) {
                        startConsumer("mqts-redo-kafka-thread", queue, 1, redoTopics, handlers.firstOrNull { it.type == IKafkaRecordHandler.HandlerType.Redo })
                        logger.info("MQTS subscribe for redo (${redoTopics.size}): ${redoTopics.joinToString(", ")}")
                    }
                }
            }
        }
    }

    private fun startConsumer(
            threadName: String,
            queue: Map.Entry<String, MutableSet<String>>,
            workPoolSize: Int,
            topics: Collection<String>,
            handler: IKafkaRecordHandler?) {
        if (handler != null && topics.isNotEmpty()) {

            val maxPoolIntervalMills = maxPollIntervalMills(topics)

            val consumer = startConsumer(queue.key, topics, handler)
            var sleepMills = 100L
            var latestWarnMills = 0L
            var warned = false

            thread(isDaemon = true, name = threadName) {
                val size = 2.coerceAtLeast(workPoolSize)
                val atomicLong = AtomicInteger(0)
                val threadPool = ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS,
                        LinkedBlockingQueue(),
                        NamedThreadFactory(isDaemon = false) { i -> "mqts-${handler.type.toString().toLowerCase()}-handling-$i" })
                try {
                    while (isStarted) {
                        val records = consumer.pollWithoutException()
                        if (!records.isEmpty) {
                            sleepMills = 100L
                            latestWarnMills = 0
                            warned = false
                            records.forEach {
                                atomicLong.incrementAndGet()
                                threadPool.submit {
                                    try {
                                        handler.handle(queue.key, it)
                                    } catch (ex: Throwable) {
                                        val invocationTargetException = ex as? InvocationTargetException
                                        val exception = if (invocationTargetException != null) invocationTargetException.targetException else ex
                                        logger.error("${handler.type} process fault, transaction topic: '${it.topic()}', thread-id: ${Thread.currentThread().id} .", exception)
                                        ex.throwIfNecessary()
                                    } finally {
                                        atomicLong.decrementAndGet()
                                    }
                                }
                            }
                            commitOffset(consumer, false)
                            this.waitForBacklogs(records, atomicLong, threadPool, handler, maxPoolIntervalMills)

                        } else {

                            Thread.sleep(sleepMills)
                            latestWarnMills += sleepMills

                            if ((latestWarnMills) > 600 * 1000) { //闲置超过 10 分钟，开始降低 poll 频率
                                sleepMills = Duration.ofSeconds(10).toMillis()
                                if (!warned) {
                                    logger.warn("${handler.type} consumer polled empty content more than ${Duration.ofMillis(latestWarnMills).toMinutes()} minutes, maybe it has become a zombie.")
                                    warned = true
                                }
                            }

                        }
                    }
                } catch (ex: Throwable) {
                    logger.error("MQTS participant ( kafka consumer ) loop stopped, queue: ${queue.key}", ex)
                    ex.throwIfNecessary()
                } finally {
                    this.consumers.remove(consumer)
                    consumer.closeWithoutException()
                    threadPool.shutdown()
                }
            }
        }
    }

    private fun startConsumer(queue: String, topics: Collection<String>, handler: IKafkaRecordHandler): KafkaConsumer<Long, ByteArray> {
        val newConsumer = createConsumer(queue, topics, handler.type)
        newConsumer.subscribe(topics, object : ConsumerRebalanceListener {
            override fun onPartitionsAssigned(partitions: MutableCollection<TopicPartition>?) {
            }

            override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>?) {
            }

        })
        this.consumers.add(newConsumer)
        return newConsumer
    }

    private fun waitForBacklogs(
            records: ConsumerRecords<*, *>,
            backlogs: AtomicInteger,
            threadPool: ThreadPoolExecutor,
            handler: IKafkaRecordHandler,
            maxPoolIntervalMills: Int) {

        if (backlogs.get() > (threadPool.maximumPoolSize * 5)) {

            val type = handler.type.toString()

            var latestOutput = 0L
            var sleepingMills: Long = 0L

            while (backlogs.get() > 0) {
                Thread.sleep(1000)
                sleepingMills += 1000

                if (sleepingMills - latestOutput >= (60 * 1000)) {
                    latestOutput = sleepingMills
                    if (logger.isWarnEnabled) {
                        logger.warn("Too many mqts ${handler.type.toString().toLowerCase()} message backlogs waiting for consuming  has been more than ${sleepingMills / (60 * 1000L)} minutes. " + System.lineSeparator() +
                                "backlogs: ${backlogs.get()}" + System.lineSeparator() +
                                "polled count:${records.count()}" + System.lineSeparator() +
                                "consumer type: ${type.toLowerCase()}" + System.lineSeparator() +
                                "active threads: ${threadPool.activeCount}" + System.lineSeparator() +
                                "work pool size: ${threadPool.maximumPoolSize}" + System.lineSeparator() +
                                "max poll interval: ${maxPoolIntervalMills}ms " + System.lineSeparator())
                    }
                }
            }
        }
    }

    private fun commitOffset(consumer: KafkaConsumer<Long, ByteArray>, async: Boolean) {
        try {
            if (async) consumer.commitAsync { _, exception ->
                if (exception != null) {
                    logger.error("Commit kafka offset fault.", exception)
                    exception.throwIfNecessary()
                }
            } else consumer.commitSync()
        } catch (ex: Throwable) {
            logger.error("Commit kafka offset fault.", ex)
            ex.throwIfNecessary()
        }
    }

    private fun KafkaConsumer<Long, ByteArray>.closeWithoutException() {
        return try {
            this.close()
        } catch (ex: Throwable) {
            logger.error("Close kafka consumer fault.", ex)
            ex.throwIfNecessary()
        }
    }

    private fun KafkaConsumer<Long, ByteArray>.pollWithoutException(timeout: Duration = Duration.ofSeconds(5L)): ConsumerRecords<Long, ByteArray> {
        return try {
            this.poll(timeout)
        } catch (ex: Throwable) {
            logger.error("Poll kafka message fault.", ex)
            ex.throwIfNecessary()
            ConsumerRecords.empty()
        }
    }

    private fun getAckRedoTopics(queue: Map.Entry<String, MutableSet<String>>): List<String> {
        return queue.value.filter {
            it.startsWith(Constants.TRANSACTION_TOPIC_PREFIX) && it.endsWith(Constants.REDO_TOPIC_STUFFIX)
        }
    }

    private fun getAckTopics(queue: Map.Entry<String, MutableSet<String>>): List<String> {
        return queue.value.filter {
            it.startsWith(Constants.TRANSACTION_TOPIC_PREFIX) && it.endsWith(Constants.ACK_TOPIC_STUFFIX)
        }
    }

    private fun getParticipantTopics(queue: Map.Entry<String, MutableSet<String>>): List<String> {
        return queue.value.filter {
            it.startsWith(Constants.TRANSACTION_TOPIC_PREFIX) && !it.endsWith(Constants.ACK_TOPIC_STUFFIX) && !it.endsWith(Constants.REDO_TOPIC_STUFFIX)
        }
    }
}
