package com.labijie.infra.mqts.spring.interceptor.Aspect

import com.labijie.infra.mqts.*
import com.labijie.infra.mqts.abstractions.IAckClient
import com.labijie.infra.mqts.abstractions.IAckServer
import com.labijie.infra.mqts.abstractions.ITransactionDataSerializer
import com.labijie.infra.mqts.spring.interceptor.MQTransactionParticipantSyncAdapter
import com.labijie.infra.mqts.spring.interceptor.MQTransactionSelectionHolder
import com.labijie.infra.mqts.spring.interceptor.MQTransactionSourceSyncAdapter
import com.labijie.infra.utils.ifNullOrBlank
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import kotlin.reflect.jvm.kotlinFunction

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-10
 */
@Aspect
class MQTransactionAspect(private val applicationContext: ApplicationContext) {
    companion object {
        private val logger by lazy { LoggerFactory.getLogger(MQTransactionAspect::class.java) }
    }

    @Pointcut("@annotation(com.labijie.infra.mqts.MQTransactionSource)")
    private fun pointCut() {
    }

    @Pointcut("@annotation(com.labijie.infra.mqts.MQTransactionSources)")
    private fun pointCut2() {
    }

    @Pointcut("@annotation(com.labijie.infra.mqts.MQTransactionParticipant)")
    private fun pointCut3() {

    }

    private val ackServer: IAckServer by lazy {
        this.applicationContext.getBean(IAckServer::class.java)
    }

    private val ackClient: IAckClient by lazy {
        this.applicationContext.getBean(IAckClient::class.java)
    }

    private val mqTransactionManager: MQTransactionManager by lazy {
        this.applicationContext.getBean(MQTransactionManager::class.java)
    }

    private val dataSerializer: ITransactionDataSerializer by lazy {
        this.applicationContext.getBean(ITransactionDataSerializer::class.java)
    }

    private val transactionManager: PlatformTransactionManager by lazy {
        this.applicationContext.getBean(PlatformTransactionManager::class.java)
    }

    private val transactionTemplate: TransactionTemplate by lazy {
        this.applicationContext.getBean(TransactionTemplate::class.java)
    }

    private fun isInTransaction(): Boolean {
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_MANDATORY
        try {
            val status = this.transactionManager.getTransaction(def)
            return (!status.isCompleted && !status.isRollbackOnly)
        } catch (ex: IllegalTransactionStateException) {
            return false
        }
    }

    @Around("pointCut()||pointCut2()||pointCut3()")
    fun around(joinPoint: JoinPoint): Any? {

        val point = joinPoint as ProceedingJoinPoint
        val method = (point.signature as MethodSignature).method

        //val anno = AnnotatedElementUtils.findMergedAnnotation(method, ::class.java)

        val identifier = TransactionIdentifier(method.declaringClass.kotlin, method.kotlinFunction!!)

        val currentTransaction = this.mqTransactionManager.getCurrentTransaction()

        val sources =  this.mqTransactionManager.findSource(identifier).values.map { it.annotation }
        val participant: TransactionParticipantAttribute? = if (currentTransaction != null) this.mqTransactionManager.findParticipant(identifier).getOrDefault(currentTransaction.transactionType, null) else null

        //再又是参与者又是事务源的情况下考虑幂等性不应该对同一个事务发起多次，因此事务源代码放在业务代码之后执行
        return if (currentTransaction != null && participant != null) {
            this.transactionTemplate.execute {
                val inTransaction = isInTransaction()
                if (inTransaction) {
                    processParticipant(currentTransaction, participant.info)
                }
                val returnValue = point.proceed(point.args)
                processSource(sources, returnValue)
                returnValue
            }
        } else {
            val returnValue = point.proceed(point.args)
            processSource(sources, returnValue)
            returnValue
        }
    }

    private fun processSource(sources: Collection<ISourceInfo>, returnValue: Any?) {
        val currentTransaction = this.mqTransactionManager.getCurrentTransaction()
        if (sources.isNotEmpty()) {
            val selectedTransactions = MQTransactionSelectionHolder.DEFAULT.currentTransactions
            val filteredSources = if(selectedTransactions.isEmpty()) sources else sources.filter {selectedTransactions.contains(it.type) }
            if (filteredSources.isNotEmpty()) {
                this.transactionTemplate.execute {
                    filteredSources.forEach { source ->
                        val buffer = if (returnValue != null) this.dataSerializer.serialize(returnValue) else null
                        val scope = this.mqTransactionManager.prepareNewTransaction(source, buffer, currentTransaction?.copy(), returnValue)
                        //不能简单调用 mqTransactionManager, 仅当事务成功时才向消息队列发送消息
                        TransactionSynchronizationManager
                                .registerSynchronization(MQTransactionSourceSyncAdapter(ackServer, this.mqTransactionManager, scope))
                    }
                }
            }
        }
    }

    private fun processParticipant(currentTransaction: MQTransaction, participant: IParticipantInfo) {
        //该方法只能在事务环境下执行，否则幂等操作将无效
        var participantSyncAdapter: MQTransactionParticipantSyncAdapter? = null
        try {
            participantSyncAdapter = MQTransactionParticipantSyncAdapter(currentTransaction, ackClient,  this.mqTransactionManager, participant)
            TransactionSynchronizationManager.registerSynchronization(participantSyncAdapter)
            if (participant.autoIdempotent) {
                this.mqTransactionManager.idempotence.ensureIdempotence(currentTransaction.copy())
            }
        } catch (iex: IdempotenceException) {
            logger.debug("Transaction with type '${participant.type.ifNullOrBlank("<null>")}' has been redo.")
            participantSyncAdapter?.idempotence = true
            throw iex
        }
    }

}