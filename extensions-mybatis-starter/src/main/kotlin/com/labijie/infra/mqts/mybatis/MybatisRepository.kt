package com.labijie.infra.mqts.mybatis

import com.fasterxml.jackson.core.type.TypeReference
import com.labijie.infra.json.JacksonHelper
import com.labijie.infra.mqts.ISourceInfo
import com.labijie.infra.mqts.IdempotenceException
import com.labijie.infra.mqts.MQTransaction
import com.labijie.infra.mqts.abstractions.IIdempotence
import com.labijie.infra.mqts.abstractions.ITransactionRepository
import com.labijie.infra.mqts.mybatis.domain.DeletedTransactionSourceModelRecord
import com.labijie.infra.mqts.mybatis.domain.TransactionParticipantModelRecord
import com.labijie.infra.mqts.mybatis.domain.TransactionSourceExpiredModelRecord
import com.labijie.infra.mqts.mybatis.domain.TransactionSourceModelRecord
import com.labijie.infra.mqts.mybatis.mapper.*
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.where.AbstractWhereDSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-12-25
 */
open class MybatisRepository(private val applicationName: String, private val transactionTemplate: TransactionTemplate) : ITransactionRepository, IIdempotence {

    companion object {
        const val MAX_FETCH_COUNT = 1000

        private fun TransactionSourceModelRecord.toMQTransaction(): MQTransaction {
            val parentId: Long? = if ((this.parentTransactionId ?: 0) <= 0L) null else this.parentTransactionId
            val transaction = MQTransaction(
                    this.transactionId!!,
                    this.transactionType!!,
                    this.timeCreated!!,
                    this.timeExpired!!,
                    this.data,
                    this.ackHostAndPort!!,
                    parentId)
            transaction.version = this.version!!
            if (!this.states.isNullOrBlank()) {
                transaction.states = JacksonHelper.deserializeFromString(this.states!!, object : TypeReference<HashMap<String, String>>() {})
            }
            return transaction
        }

        private fun fromTransaction(applicationName: String, transaction: MQTransaction): TransactionSourceModelRecord {
            return TransactionSourceModelRecord().apply {
                transactionId = transaction.transactionId
                transactionType = transaction.transactionType
                parentTransactionId = transaction.parentTransactionId ?: 0
                timeCreated = transaction.timeCreated
                timeExpired = transaction.timeExpired
                data = transaction.data
                ackHostAndPort = transaction.ackHostAndPort
                version = transaction.version
                states = JacksonHelper.serializeAsString(transaction.states)
                this.applicationName = applicationName
            }
        }

        fun fromExpired(expired: TransactionSourceExpiredModelRecord, timeoutSeconds: Int): TransactionSourceModelRecord {
            val data = TransactionSourceModelRecord().apply {
                this.transactionId = expired.transactionId
                this.ackHostAndPort = expired.ackHostAndPort
                this.applicationName = expired.applicationName
                this.parentTransactionId = expired.parentTransactionId
                this.timeExpired = System.currentTimeMillis() + timeoutSeconds * 1000
                this.timeCreated = expired.timeCreated
                this.version = expired.version
                this.states = expired.states
                this.transactionType = expired.transactionType
                this.data = expired.data?.toByteArray(Charsets.UTF_8)
            }
            return data
        }

        fun TransactionSourceModelRecord.toDeleted(): DeletedTransactionSourceModelRecord {
            return DeletedTransactionSourceModelRecord().also {
                it.transactionId = this.transactionId
                it.ackHostAndPort = this.ackHostAndPort
                it.applicationName = this.applicationName
                it.parentTransactionId = this.parentTransactionId
                it.timeCreated = this.timeCreated
                it.timeExpired = this.timeExpired
                it.transactionType = this.transactionType
                it.version = this.version
                it.data = this.data
                it.states = this.states
            }
        }

        fun TransactionSourceModelRecord.toExpired(): TransactionSourceExpiredModelRecord {
            val data = TransactionSourceExpiredModelRecord().also {
                it.transactionId = this.transactionId
                it.ackHostAndPort = this.ackHostAndPort
                it.applicationName = this.applicationName
                it.parentTransactionId = this.parentTransactionId
                it.timeCreated = this.timeCreated
                it.version = this.version
                it.states = this.states
                it.timeExpired = this.timeExpired
                it.transactionType = this.transactionType
                it.data = (this.data?.toString(Charsets.UTF_8)).orEmpty()
            }
            return data
        }

        fun String.delimiter(): String {
            return "`$this`"
        }
    }

    @Autowired
    private lateinit var sourceMapper: TransactionSourceModelMapper

    @Autowired
    private lateinit var participantMapper: TransactionParticipantModelMapper

    @Autowired
    private lateinit var expiredMapper: TransactionSourceExpiredModelMapper

    @Autowired
    private lateinit var deletedSourceMapper: DeletedTransactionSourceModelMapper

    override fun ensureIdempotence(transaction: MQTransaction) {
        try {
            this.transactionTemplate.execute {
                val transactionParticipantRecord = TransactionParticipantModelRecord().also {
                    it.transactionId = transaction.transactionId
                    it.ackHostAndPort = transaction.ackHostAndPort
                    it.applicationName = this.applicationName
                    it.parentTransactionId = transaction.parentTransactionId ?: 0
                    it.timeCreated = transaction.timeCreated
                    it.timeExpired = transaction.timeExpired
                    it.transactionType = transaction.transactionType
                    it.version = transaction.version
                    it.timeInserted = System.currentTimeMillis()
                }
                this.participantMapper.insert(transactionParticipantRecord)
            }
        } catch (ex: DuplicateKeyException) {
            throw IdempotenceException("Participant impatience, transactionId: ${transaction.transactionId}")
        }
    }

    override fun getAvailable(): Array<out MQTransaction> {
        val itemList = ArrayList<TransactionSourceModelRecord>()
        val now = System.currentTimeMillis()
        val queryBuilder = { where: AbstractWhereDSL<*> ->
            where.and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.timeCreated, SqlBuilder.isLessThan(now))
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName, SqlBuilder.isEqualTo(this.applicationName))
        }

        this.transactionTemplate.execute {
            selectAllByExample(queryBuilder) { itemList.addAll(it) }
        }

        return itemList.map { it.toMQTransaction() }.toTypedArray()
    }

    private fun selectAllByExample(
            whereBuilder: (where: AbstractWhereDSL<*>) -> AbstractWhereDSL<*>,
            process: (entities: List<TransactionSourceModelRecord>) -> Unit) {

        val countSelect = SqlBuilder.countFrom(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel)
                .where().apply {
                    whereBuilder(this)
                }
                .build()
                .render(RenderingStrategies.MYBATIS3)

        val selectFrom = {
            SqlBuilder
                    .select(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.allColumns())
                    .from(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel)
                    .where()
        }

        val select = selectFrom().apply {
            whereBuilder(this)
        }
                .orderBy(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId.descending())
                .build()
                .render(RenderingStrategies.MYBATIS3)


        val count = sourceMapper.count(countSelect)
        val totalPages = if ((count % MAX_FETCH_COUNT) > 0) (count / MAX_FETCH_COUNT) + 1 else count / MAX_FETCH_COUNT

        if (count <= MAX_FETCH_COUNT) {
            val entities = sourceMapper.selectMany(select)
            process.invoke(entities)
        } else {
            var startIndex = 0
            var latestId: Long? = null

            while (startIndex < totalPages) {
                val selectQuery = selectFrom().apply {
                    whereBuilder(this)
                }
                .apply {
                    if (latestId != null) {
                        this.and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId, SqlBuilder.isLessThan(latestId))
                    }
                    this.limit(MAX_FETCH_COUNT.toLong())
                }.build().render(RenderingStrategies.MYBATIS3)

                val entities = sourceMapper.selectMany(selectQuery)
                latestId = entities.lastOrNull()?.transactionId
                process.invoke(entities)
                startIndex++
            }
        }
    }

    @Transactional
    override fun deleteByTransactionId(transactionId: Long): Boolean {
        val appName = this.applicationName
        val source = sourceMapper.selectOne {
            this.where(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId, SqlBuilder.isEqualTo(transactionId))
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName, SqlBuilder.isEqualTo(appName))
        }
        if (source != null) {
            val deletedCount = sourceMapper.deleteByPrimaryKey(source.transactionId!!)
            if (deletedCount == 1) {
                val backup = source.toDeleted()
                deletedSourceMapper.insert(backup)
            }
            return true
        }
        return false
    }

    @Transactional
    override fun expireByTransactionId(transactionId: Long): Boolean {
        val appName = this.applicationName
        val source = sourceMapper.selectOne {
            this.where(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId, SqlBuilder.isEqualTo(transactionId))
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName, SqlBuilder.isEqualTo(appName))
        }
        if (source != null) {
            expiredSource(source)
            return true
        }
        return false
    }

    private fun expiredSource(transaction: TransactionSourceModelRecord) {
        this.sourceMapper.deleteByPrimaryKey(transaction.transactionId!!)
        val expired = transaction.toExpired()
        this.expiredMapper.insert(expired)
    }

    @Transactional(readOnly = true)
    override fun getById(transactionId: Long): MQTransaction? {
        val appName = this.applicationName
        val data = sourceMapper.selectOne {
            this.where(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId, SqlBuilder.isEqualTo(transactionId))
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName, SqlBuilder.isEqualTo(appName))
        }
        return data?.toMQTransaction()
    }

    @Transactional(readOnly = true)
    override fun isTransactionExisted(transactionId: Long): Boolean {
        val appName = this.applicationName
        val count = sourceMapper.count {
            this.where(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId, SqlBuilder.isEqualTo(transactionId))
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName, SqlBuilder.isEqualTo(appName))
        }
        return count > 0
    }

    @Transactional
    @Throws(IdempotenceException::class)
    override fun recoverExpiredTransaction(transactionId: Long, timeoutSeconds: Int): MQTransaction? {
        val appName = this.applicationName
        val expired = this.expiredMapper.selectOne {
            this.where(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId, SqlBuilder.isEqualTo(transactionId))
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName, SqlBuilder.isEqualTo(appName))
        }
        if (expired != null) {
            this.expiredMapper.deleteByPrimaryKey(transactionId)
            val data = fromExpired(expired, timeoutSeconds)
            this.sourceMapper.insert(data)
            return data.toMQTransaction()
        } else {
            return null
        }
    }


    @Transactional
    override fun clearExpired() {
        val now = System.currentTimeMillis()

        val whereBuilder = { where: AbstractWhereDSL<*> ->
            where
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName, SqlBuilder.isEqualTo(this.applicationName))
                    .and(TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.timeExpired, SqlBuilder.isLessThanOrEqualTo(now))
        }

        this.selectAllByExample(whereBuilder = whereBuilder) { list ->
            list.forEach {
                this.expiredSource(it)
            }
        }
    }

    @Transactional
    override fun save(mqTransaction: MQTransaction, attribute: ISourceInfo) {
        val model = fromTransaction(this.applicationName, mqTransaction)
        this.sourceMapper.insert(model)
    }
}