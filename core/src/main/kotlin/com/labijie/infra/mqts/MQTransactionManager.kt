package com.labijie.infra.mqts

import com.labijie.infra.IIdGenerator
import com.labijie.infra.impl.DebugIdGenerator
import io.netty.util.HashedWheelTimer
import com.labijie.infra.mqts.impl.DefaultTransactionHolder
import com.labijie.infra.mqts.impl.JacksonDataSerializer
import com.labijie.infra.mqts.impl.MemoryTransactionQueue
import com.labijie.infra.mqts.impl.SimpleInstanceFactory
import com.labijie.infra.mqts.abstractions.*
import com.labijie.infra.mqts.ack.AckRequest
import com.labijie.infra.mqts.ack.AckRequestContext
import com.labijie.infra.mqts.ack.CallbackKey
import com.labijie.infra.mqts.configuration.MQTransactionConfig
import com.labijie.infra.mqts.context.TransactionContext
import com.labijie.infra.utils.throwIfNecessary
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.net.URI
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.HashSet
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


class MQTransactionManager constructor(
        val idempotence: IIdempotence,
        private val repository: ITransactionRepository,
        private val dataSerializer: ITransactionDataSerializer = JacksonDataSerializer(),
        private val idGenerator: IIdGenerator = DebugIdGenerator(),
        private val config: MQTransactionConfig = MQTransactionConfig(),
        private val instanceFactory: IInstanceFactory = SimpleInstanceFactory(),
        private val transactionHolder: ITransactionHolder = DefaultTransactionHolder(),
        private val mq: ITransactionQueue = MemoryTransactionQueue(transactionHolder),
        private val host: String = "",
        val isDevelopment: Boolean = false) {

    val transactionListeners = TransactionListeners()

    private val sourceSync: Any = Any()
    private val participantSync: Any = Any()
    private val retryTimer: HashedWheelTimer
    private val transactionAttributes = HashMap<String, TransactionSourceAttribute>()

    private val sourceAttributes = HashMap<TransactionIdentifier, MutableMap<String, TransactionSourceAttribute>>()
    private val participantAttributes = HashMap<TransactionIdentifier, MutableMap<String, TransactionParticipantAttribute>>()
    private val callbackSet = HashMap<CallbackKey, HashSet<ITransactionAckCallback>>()

    private val retryTasks: ConcurrentHashMap<String, ConcurrentHashMap<Long, TransactionProducingContext>> = ConcurrentHashMap()
    private val retryThreadCount = AtomicLong(0)


    val transactionSources: Collection<TransactionSourceAttribute>
        get() = this.transactionAttributes.values

    init {
        val s = System.getSecurityManager()
        val group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup

        this.retryTimer = HashedWheelTimer(ThreadFactory { r ->
            Thread(group, r,
                    "mqts-retry-${retryThreadCount.incrementAndGet()}")
                    .apply {
                        if (this.isDaemon)
                            this.isDaemon = false
                        if (this.priority != Thread.NORM_PRIORITY)
                            this.priority = Thread.NORM_PRIORITY
                    }
        }, 1, TimeUnit.SECONDS)

        Runtime.getRuntime().addShutdownHook(Thread {
            this.retryTimer.stop()
        })
    }

    companion object {
        //val PARENT_TRANSACTION_STATE = "__parent"
        const val PAYLOAD_STATE_PREFIX = "__pl:"
        val DEFAULT_RETRY_TICKS = intArrayOf(5, 10, 10, 30, 60)

        val logger = LoggerFactory.getLogger(MQTransactionManager::class.java)
    }


    @Throws(MQTransactionException::class)
    fun findParticipant(identifier: TransactionIdentifier): Map<String, TransactionParticipantAttribute> {
        if (!this.participantAttributes.containsKey(identifier)) {
            return mapOf()
        }
        return this.participantAttributes[identifier]!!.toMap()
    }

    @Throws(MQTransactionException::class)
    fun findSource(identifier: TransactionIdentifier): Map<String, TransactionSourceAttribute> {
        if (!this.sourceAttributes.containsKey(identifier)) {
            return mutableMapOf()
        }
        return this.sourceAttributes[identifier]!!.toMap()
    }

    fun registerListeners(vararg listener: ITransactionListener) {
        if (listener.isNotEmpty()) {
            this.transactionListeners.addAll(listener)
        }
    }

    fun stopAllRetry() {
        this.retryTimer.stop()
        this.retryTasks.values.forEach { tasks ->
            tasks.forEach {
                it.value.cancel()
            }
        }
        this.retryTasks.clear()
        logger.info("MQ transaction manager was shutdown.")
    }


    @Throws(MQTransactionException::class)
    private fun getTransactionAttribute(type: String, throwIfNotFound: Boolean = true): TransactionSourceAttribute? {
        if (sourceAttributes.isEmpty()) {
            if (throwIfNotFound) throw MQTransactionException("cant find any transaction attributes") else logger.warn("cant find transaction type '$type' in code, but it was in transaction storage")
        }
        val attribute = this.transactionAttributes[type]
        if (attribute == null) {
            if (throwIfNotFound) throw MQTransactionException("cant find transaction type '$type'") else logger.warn("cant find transaction type '$type' in code, but it was in transaction storage")
        }
        return attribute
    }


    /**
     * 启用事务源
     */
    fun enableSource(vararg sources: TransactionSourceAttribute) {
        synchronized(this.sourceSync) {
            if (sources.isNotEmpty()) {
                this.retryTimer.start()

                val typeNames = HashSet<String>()
                sources.forEach {
                    val identifier = TransactionIdentifier(it.declareClass, it.method)
                    val map = this.sourceAttributes.getOrPut(identifier) { mutableMapOf<String, TransactionSourceAttribute>() }
                    map.putIfAbsent(it.annotation.type, it)

                    if (it.annotation.ackCallback != null) {
                        val callback = it.annotation.ackCallback!!
                        if (!callback.isSubclassOf(ITransactionAckCallback::class)) {
                            throw MQTransactionException("The ackCallback property of MQTransactionSource annotation must be a ITransactionAckCallback type")
                        }

                        //类型和回调类型去重
                        if (it.annotation.ackCallback != null && typeNames.add("${it.annotation.type}_${callback.simpleName}")) {
                            val callbackInstance = this.instanceFactory.createInstance(it.annotation.ackCallback!!) as ITransactionAckCallback
                            val set = callbackSet.getOrPut(CallbackKey(it.annotation.queue, it.annotation.type)) { HashSet() }
                            set.add(callbackInstance)
                        }
                    }

                    if (this.transactionAttributes.containsKey(it.annotation.type)) {
                        val existed = this.transactionAttributes[it.annotation.type]
                        if (existed != null && existed.configEquals(it)) {
                            logger.warn("Transaction with type '${it.annotation.type}' used different configuration")
                        }
                    }
                    this.transactionAttributes[it.annotation.type] = it
                }
            }
        }
    }


    fun recoveryTransactions(ackServer: IAckServer): RecoveryResult {
        try {
            var recovered: Int = 0
            var expired: Int = 0
            val tran = repository.getAvailable()
            tran.forEach {
                //如果没有过期
                val sourceAttribute = getTransactionAttribute(it.transactionType, throwIfNotFound = false)
                if (sourceAttribute != null) {
                    val scopeContext = TransactionContext(it)
                    if (!it.isExpired(sourceAttribute)) {
                        this.beginTransaction(ackServer, scopeContext, isReload = true)
                        recovered ++
                    } else {
                        this.expireTransaction(scopeContext)
                        logger.warn("Transaction ('${it.transactionId}') has expired")
                        expired++
                    }
                }
            }
            return RecoveryResult(recovered, expired)
        } catch (ex: Throwable) {
            logger.error("An error occurred while initializing mq transaction source", ex)
            throw ex
        }
    }

    /**
     * 启用事务参与者
     */
    fun enableParticipant(vararg participants: TransactionParticipantAttribute) {
        synchronized(this.participantSync) {
            participants.forEach {

                val identifier = TransactionIdentifier(it.declareClass, it.method)
                val map = this.participantAttributes.getOrPut(identifier) { mutableMapOf() }
                map.putIfAbsent(it.info.type, it)

                if (it.method.parameters.size !in listOf(1, 2)) {
                    throw MQTransactionException("transaction participant method can contain only one parameter or zero parameter.")
                }

                val instance = this.instanceFactory.createInstance(it.declareClass)

                mq.registerHandler(it.info.queue, it.info.type) { mqt ->
                    run {
                        if (mqt.transactionType.compareTo(it.info.type, true) == 0) {
                            val scopeContext = TransactionContext(mqt)
                            this.transactionListeners.onTransactionExecuting(scopeContext)
                            try {
                                if (mqt.data == null) {
                                    if (it.method.parameters.size == 2) {
                                        throw MQTransactionException("Transaction participant method without parameter, but got one.")
                                    }
                                    it.method.call(instance)
                                } else {
                                    if (it.method.parameters.size == 1) {
                                        throw MQTransactionException("Transaction participant method need at least one parameter.")
                                    }
                                    val dataType = (it.method.parameters[1].type.classifier as KClass<out Any>)
                                    val data: Any? = try {
                                        this.dataSerializer.deserialize(mqt.data!!, dataType)
                                    } catch (ex: Throwable) {
                                        scopeContext.exception = ex
                                        logger.error("Input data of participant cannot be deserialized, considering that the input and output data types of transaction source and participant do not match ( transaction-type:${it.info.type} ) .", ex)
                                        ex.throwIfNecessary()
                                        return@run
                                    }
                                    it.method.call(instance, data)
                                }
                            } catch (ex: InvocationTargetException) {
                                if (ex.cause !is IdempotenceException) {
                                    scopeContext.exception = if (ex.cause != null) ex.cause else ex
                                    throw ex
                                }
                            } catch (ex: Throwable) {
                                scopeContext.exception = ex
                                throw ex
                            } finally {
                                this.transactionListeners.onTransactionExecuted(scopeContext)
                            }
                        }
                    }
                }
            }
        }
    }


    fun getCurrentTransaction(): MQTransaction? {
        return this.transactionHolder.currentTransaction
    }

    fun findTransaction(transactionId: Long): MQTransaction? {
        return this.repository.getById(transactionId)
    }

    fun recoverExpiredTransaction(transactionId: Long, transactionType: String): MQTransaction? {
        val source = getTransactionAttribute(transactionType, true)!!
        return this.repository.recoverExpiredTransaction(transactionId, source.annotation.timeoutSeconds)
    }

    fun prepareNewTransaction(source: ISourceInfo,
                              data: ByteArray? = null,
                              parentTransaction: MQTransaction? = null,
                              returnValue: Any?): TransactionContext {
        val id = this.idGenerator.newId()
        val now = System.currentTimeMillis()

        val mqTransaction = MQTransaction(id,
                source.type,
                now,
                now + source.timeoutSeconds * 1000,
                data,
                host,
                parentTransaction?.transactionId)


        val payload = returnValue as? IMQTransactionPayload

        payload?.extract()?.forEach {
            mqTransaction.states["$PAYLOAD_STATE_PREFIX${it.key}"] = it.value
        }

//        if (parentTransaction != null) {
//            mqTransaction.states[PARENT_TRANSACTION_STATE] = ObjectMapper().writeValueAsString(parentTransaction)
//        }

        val scopeContext = TransactionContext(mqTransaction, parentTransaction, returnValue)
        this.transactionListeners.onTransactionPrepared(scopeContext)
        this.repository.save(mqTransaction, source)
        return scopeContext
    }

    /**
     * 开始一个事务
     * @param ackServer 负责事务 ack 应答的服务器
     * @param transactionScopeContext 要开始的事务对象
     * @param sourceAttribute 事务相关的属性（元数据）
     */
    fun beginTransaction(ackServer: IAckServer, transactionScopeContext: TransactionContext, sourceAttribute: TransactionSourceAttribute? = null) {
        this.beginTransaction(ackServer, transactionScopeContext, sourceAttribute, false)
    }


    private fun throwIfOutOfSize(transaction: MQTransaction) {
        if (transaction.data != null) {
            if (transaction.data!!.size > this.config.maxDataSizeBytes) {
                throw MQTransactionException("Mqts data exceeded limit ( max size: ${config.maxDataSizeBytes} bytes)")
            }
        }
    }

    private fun beginTransaction(
            ackServer: IAckServer,
            transactionScopeContext: TransactionContext,
            sourceAttribute: TransactionSourceAttribute? = null,
            isReload: Boolean = false) {

        val transaction = transactionScopeContext.transaction
        this.throwIfOutOfSize(transaction)

        //恢复事务时候可能发生源地址变化
        transaction.ackHostAndPort = ackServer.getAckAddress().toString()

        var source = sourceAttribute
        if (sourceAttribute == null) {
            source = getTransactionAttribute(transaction.transactionType)
        }
        //retryTimer.newTimeout()
        val context = TransactionProducingContext(this.mq, transactionScopeContext.transaction, source!!, !isReload, transactionScopeContext.states)

        val taskMap = this.retryTasks.getOrPut(context.transaction.transactionType) { ConcurrentHashMap() }
        if (taskMap.putIfAbsent(context.transaction.transactionId, context) == null) {
            if (!this.isTransactionExisted(context.transaction.transactionId)) {
                this.removeTransactionRetryTask(transactionScopeContext.transaction)
                logger.error("Transaction with id '${context.transaction.transactionId}' was ready to begin, but it was not existed.")
                return
            }
            val execution = TransactionProducer(this, this.retryTimer, context, this.transactionListeners)
            execution.execute()
        } else {
            logger.warn("Cant to begin a transaction '${context.transaction.transactionId}' that existed in retry task.")
        }
    }

    /**
     * 完成一个事务（清理重试任务，并提交数据）
     */
    private fun commitTransaction(context: TransactionContext) {

        try {
            if (!this.repository.deleteByTransactionId(context.transaction.transactionId)) {
                context.idempotent = true
                logger.debug("Redo complete transaction.")
            }
        } catch (ide: IdempotenceException) {
            context.idempotent = true
        } catch (ex: Throwable) {
            context.exception = ex
        } finally {
            if (!removeTransactionRetryTask(context.transaction)) {
                logger.debug("MQTS retry task cancel failed, possibly because the task has started ( tran-id:${context.transaction.transactionId}, type:${context.transaction.transactionType} )")
            } else {
                logger.debug("MQTS retry task was canceled.")
            }
            this.transactionListeners.onTransactionCompleted(context)
        }
    }

    internal fun removeTransactionRetryTask(transaction: MQTransaction): Boolean {
        val set = this.retryTasks.getOrPut(transaction.transactionType) { ConcurrentHashMap() }
        val pctx = set.remove(transaction.transactionId)
        pctx?.cancel()
        return pctx != null
    }

    fun expireTransaction(transactionScopeContext: TransactionContext) {
        if (this.repository.expireByTransactionId(transactionScopeContext.transaction.transactionId)) {
            this.transactionListeners.onTransactionExpired(transactionScopeContext)
        }
        removeTransactionRetryTask(transactionScopeContext.transaction)
    }

    fun isTransactionExisted(transactionId: Long): Boolean {
        return try {
            this.repository.isTransactionExisted(transactionId)
        } catch (ex: Throwable) {
            //出错时候宁愿相信事务存在
            logger.warn("Execute isTransactionExisted fault.", ex)
            return true
        }
    }

    private fun transactionScopeContextFromAckRequest(transaction: MQTransaction, request: AckRequest): TransactionContext {
        val tranCtx = TransactionContext(transaction)
        request.transactionStates.forEach {
            tranCtx.transaction.states[it.key] = it.value
        }
        return tranCtx
    }

    fun completeTransaction(queue: String, ackRequest: AckRequest, result: TransactionResult) {
        val transaction = this.findTransaction(ackRequest.transactionId)
        if (transaction != null) {
            val tranCtx = transactionScopeContextFromAckRequest(transaction, ackRequest)
            this.transactionListeners.onTransactionAckReceived(tranCtx)
            val context = AckContext(transaction, this, result) {
                this.commitTransaction(tranCtx)
            }

            val callbacks = this.callbackSet.getOrDefault(CallbackKey(queue, transaction.transactionType), null)
            if (callbacks != null) {
                callbacks.forEach { c ->
                    try {
                        //手动提交
                        c.onAck(context)
                    } catch (ex: IdempotenceException) {
                        logger.warn("Redo complete transaction ( transaction id: ${transaction.transactionId} )")
                    } catch (ex: Throwable) {
                        throw ex
                    }
                }

            } else { //没有配置回调的情况下自动提交事务
                try {
                    context.commitMQTransaction()
                } catch (ex: IdempotenceException) {
                    logger.warn("Redo complete transaction ( transaction id: ${transaction.transactionId} )")
                } catch (ex: Throwable) {
                    throw ex
                }
            }
            if (!context.isCommitted) {
                logger.warn("Transaction will not be completed unless AckContext.commitTransaction method invoked.")
            }
        }

    }

    /**
     * 应答一个事务的执行结果
     */
    @Throws(MQTransactionException::class)
    fun ackTransaction(transaction: MQTransaction, ackClient: IAckClient, participant: IParticipantInfo, result: TransactionResult) {

        val context = TransactionContext(transaction)

        transactionListeners.onTransactionAckRequesting(context)
        try {
            val ackContext = AckRequestContext(URI(transaction.ackHostAndPort),
                    transaction.transactionId,
                    transaction.transactionType,
                    participant.queue,
                    transaction.extractStatesWithoutPayload())
            ackClient.write(ackContext, result)
        } catch (ex: Throwable) {
            logger.error("MQ transaction invoking ack server error.", ex)
            context.exception = ex
            ex.throwIfNecessary()

        } finally {
            transactionListeners.onTransactionAckRequested(context)
        }
    }
}