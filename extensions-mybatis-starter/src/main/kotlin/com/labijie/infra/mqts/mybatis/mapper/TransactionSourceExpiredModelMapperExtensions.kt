/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.labijie.infra.mqts.mybatis.mapper

import com.labijie.infra.mqts.mybatis.domain.TransactionSourceExpiredModelRecord
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.ackHostAndPort
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.applicationName
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.data
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.parentTransactionId
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.states
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.timeCreated
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.timeExpired
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.transactionId
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.transactionType
import com.labijie.infra.mqts.mybatis.mapper.TransactionSourceExpiredModelDynamicSqlSupport.TransactionSourceExpiredModel.version
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun TransactionSourceExpiredModelMapper.count(completer: CountCompleter) =
    countFrom(this::count, TransactionSourceExpiredModel, completer)

fun TransactionSourceExpiredModelMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, TransactionSourceExpiredModel, completer)

fun TransactionSourceExpiredModelMapper.deleteByPrimaryKey(transactionId_: Long) =
    delete {
        where(transactionId, isEqualTo(transactionId_))
    }

fun TransactionSourceExpiredModelMapper.insert(record: TransactionSourceExpiredModelRecord) =
    insert(this::insert, record, TransactionSourceExpiredModel) {
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

fun TransactionSourceExpiredModelMapper.insertMultiple(records: Collection<TransactionSourceExpiredModelRecord>) =
    insertMultiple(this::insertMultiple, records, TransactionSourceExpiredModel) {
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

fun TransactionSourceExpiredModelMapper.insertMultiple(vararg records: TransactionSourceExpiredModelRecord) =
    insertMultiple(records.toList())

fun TransactionSourceExpiredModelMapper.insertSelective(record: TransactionSourceExpiredModelRecord) =
    insert(this::insert, record, TransactionSourceExpiredModel) {
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

fun TransactionSourceExpiredModelMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, TransactionSourceExpiredModel, completer)

fun TransactionSourceExpiredModelMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, TransactionSourceExpiredModel, completer)

fun TransactionSourceExpiredModelMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, TransactionSourceExpiredModel, completer)

fun TransactionSourceExpiredModelMapper.selectByPrimaryKey(transactionId_: Long) =
    selectOne {
        where(transactionId, isEqualTo(transactionId_))
    }

fun TransactionSourceExpiredModelMapper.update(completer: UpdateCompleter) =
    update(this::update, TransactionSourceExpiredModel, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: TransactionSourceExpiredModelRecord) =
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

fun KotlinUpdateBuilder.updateSelectiveColumns(record: TransactionSourceExpiredModelRecord) =
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

fun TransactionSourceExpiredModelMapper.updateByPrimaryKey(record: TransactionSourceExpiredModelRecord) =
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

fun TransactionSourceExpiredModelMapper.updateByPrimaryKeySelective(record: TransactionSourceExpiredModelRecord) =
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