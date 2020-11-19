/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.labijie.infra.mqts.mybatis.mapper

import com.labijie.infra.mqts.mybatis.domain.TransactionSourceModelRecord
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.ackHostAndPort
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.applicationName
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.data
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.parentTransactionId
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.states
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.timeCreated
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.timeExpired
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionId
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.transactionType
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceModelDynamicSqlSupport.TransactionSourceModel.version
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun TransactionSourceModelMapper.count(completer: CountCompleter) =
    countFrom(this::count, TransactionSourceModel, completer)

fun TransactionSourceModelMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, TransactionSourceModel, completer)

fun TransactionSourceModelMapper.deleteByPrimaryKey(transactionId_: Long) =
    delete {
        where(transactionId, isEqualTo(transactionId_))
    }

fun TransactionSourceModelMapper.insert(record: TransactionSourceModelRecord) =
    insert(this::insert, record, TransactionSourceModel) {
        map(transactionId).toProperty("transactionId")
        map(ackHostAndPort).toProperty("ackHostAndPort")
        map(applicationName).toProperty("applicationName")
        map(parentTransactionId).toProperty("parentTransactionId")
        map(timeCreated).toProperty("timeCreated")
        map(timeExpired).toProperty("timeExpired")
        map(transactionType).toProperty("transactionType")
        map(version).toProperty("version")
        map(data).toProperty("data")
        map(states).toProperty("states")
    }

fun TransactionSourceModelMapper.insertMultiple(records: Collection<TransactionSourceModelRecord>) =
    insertMultiple(this::insertMultiple, records, TransactionSourceModel) {
        map(transactionId).toProperty("transactionId")
        map(ackHostAndPort).toProperty("ackHostAndPort")
        map(applicationName).toProperty("applicationName")
        map(parentTransactionId).toProperty("parentTransactionId")
        map(timeCreated).toProperty("timeCreated")
        map(timeExpired).toProperty("timeExpired")
        map(transactionType).toProperty("transactionType")
        map(version).toProperty("version")
        map(data).toProperty("data")
        map(states).toProperty("states")
    }

fun TransactionSourceModelMapper.insertMultiple(vararg records: TransactionSourceModelRecord) =
    insertMultiple(records.toList())

fun TransactionSourceModelMapper.insertSelective(record: TransactionSourceModelRecord) =
    insert(this::insert, record, TransactionSourceModel) {
        map(transactionId).toPropertyWhenPresent("transactionId", record::transactionId)
        map(ackHostAndPort).toPropertyWhenPresent("ackHostAndPort", record::ackHostAndPort)
        map(applicationName).toPropertyWhenPresent("applicationName", record::applicationName)
        map(parentTransactionId).toPropertyWhenPresent("parentTransactionId", record::parentTransactionId)
        map(timeCreated).toPropertyWhenPresent("timeCreated", record::timeCreated)
        map(timeExpired).toPropertyWhenPresent("timeExpired", record::timeExpired)
        map(transactionType).toPropertyWhenPresent("transactionType", record::transactionType)
        map(version).toPropertyWhenPresent("version", record::version)
        map(data).toPropertyWhenPresent("data", record::data)
        map(states).toPropertyWhenPresent("states", record::states)
    }

private val columnList = listOf(transactionId, ackHostAndPort, applicationName, parentTransactionId, timeCreated, timeExpired, transactionType, version, data, states)

fun TransactionSourceModelMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, TransactionSourceModel, completer)

fun TransactionSourceModelMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, TransactionSourceModel, completer)

fun TransactionSourceModelMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, TransactionSourceModel, completer)

fun TransactionSourceModelMapper.selectByPrimaryKey(transactionId_: Long) =
    selectOne {
        where(transactionId, isEqualTo(transactionId_))
    }

fun TransactionSourceModelMapper.update(completer: UpdateCompleter) =
    update(this::update, TransactionSourceModel, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: TransactionSourceModelRecord) =
    apply {
        set(transactionId).equalTo(record::transactionId)
        set(ackHostAndPort).equalTo(record::ackHostAndPort)
        set(applicationName).equalTo(record::applicationName)
        set(parentTransactionId).equalTo(record::parentTransactionId)
        set(timeCreated).equalTo(record::timeCreated)
        set(timeExpired).equalTo(record::timeExpired)
        set(transactionType).equalTo(record::transactionType)
        set(version).equalTo(record::version)
        set(data).equalTo(record::data)
        set(states).equalTo(record::states)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: TransactionSourceModelRecord) =
    apply {
        set(transactionId).equalToWhenPresent(record::transactionId)
        set(ackHostAndPort).equalToWhenPresent(record::ackHostAndPort)
        set(applicationName).equalToWhenPresent(record::applicationName)
        set(parentTransactionId).equalToWhenPresent(record::parentTransactionId)
        set(timeCreated).equalToWhenPresent(record::timeCreated)
        set(timeExpired).equalToWhenPresent(record::timeExpired)
        set(transactionType).equalToWhenPresent(record::transactionType)
        set(version).equalToWhenPresent(record::version)
        set(data).equalToWhenPresent(record::data)
        set(states).equalToWhenPresent(record::states)
    }

fun TransactionSourceModelMapper.updateByPrimaryKey(record: TransactionSourceModelRecord) =
    update {
        set(ackHostAndPort).equalTo(record::ackHostAndPort)
        set(applicationName).equalTo(record::applicationName)
        set(parentTransactionId).equalTo(record::parentTransactionId)
        set(timeCreated).equalTo(record::timeCreated)
        set(timeExpired).equalTo(record::timeExpired)
        set(transactionType).equalTo(record::transactionType)
        set(version).equalTo(record::version)
        set(data).equalTo(record::data)
        set(states).equalTo(record::states)
        where(transactionId, isEqualTo(record::transactionId))
    }

fun TransactionSourceModelMapper.updateByPrimaryKeySelective(record: TransactionSourceModelRecord) =
    update {
        set(ackHostAndPort).equalToWhenPresent(record::ackHostAndPort)
        set(applicationName).equalToWhenPresent(record::applicationName)
        set(parentTransactionId).equalToWhenPresent(record::parentTransactionId)
        set(timeCreated).equalToWhenPresent(record::timeCreated)
        set(timeExpired).equalToWhenPresent(record::timeExpired)
        set(transactionType).equalToWhenPresent(record::transactionType)
        set(version).equalToWhenPresent(record::version)
        set(data).equalToWhenPresent(record::data)
        set(states).equalToWhenPresent(record::states)
        where(transactionId, isEqualTo(record::transactionId))
    }