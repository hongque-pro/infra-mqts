/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.labijie.infra.mqts.mybatis.mapper

import com.labijie.infra.mqts.mybatis.domain.TransactionParticipantModelRecord
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.ackHostAndPort
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.applicationName
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.parentTransactionId
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.states
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.timeCreated
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.timeExpired
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.timeInserted
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.transactionId
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.transactionType
import com.labijie.infra.mqts.mybatis.mapper.TransactionParticipantModelDynamicSqlSupport.TransactionParticipantModel.version
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun TransactionParticipantModelMapper.count(completer: CountCompleter) =
    countFrom(this::count, TransactionParticipantModel, completer)

fun TransactionParticipantModelMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, TransactionParticipantModel, completer)

fun TransactionParticipantModelMapper.deleteByPrimaryKey(transactionId_: Long) =
    delete {
        where(transactionId, isEqualTo(transactionId_))
    }

fun TransactionParticipantModelMapper.insert(record: TransactionParticipantModelRecord) =
    insert(this::insert, record, TransactionParticipantModel) {
        map(transactionId).toProperty("transactionId")
        map(ackHostAndPort).toProperty("ackHostAndPort")
        map(applicationName).toProperty("applicationName")
        map(parentTransactionId).toProperty("parentTransactionId")
        map(timeCreated).toProperty("timeCreated")
        map(timeExpired).toProperty("timeExpired")
        map(timeInserted).toProperty("timeInserted")
        map(transactionType).toProperty("transactionType")
        map(version).toProperty("version")
        map(states).toProperty("states")
    }

fun TransactionParticipantModelMapper.insertMultiple(records: Collection<TransactionParticipantModelRecord>) =
    insertMultiple(this::insertMultiple, records, TransactionParticipantModel) {
        map(transactionId).toProperty("transactionId")
        map(ackHostAndPort).toProperty("ackHostAndPort")
        map(applicationName).toProperty("applicationName")
        map(parentTransactionId).toProperty("parentTransactionId")
        map(timeCreated).toProperty("timeCreated")
        map(timeExpired).toProperty("timeExpired")
        map(timeInserted).toProperty("timeInserted")
        map(transactionType).toProperty("transactionType")
        map(version).toProperty("version")
        map(states).toProperty("states")
    }

fun TransactionParticipantModelMapper.insertMultiple(vararg records: TransactionParticipantModelRecord) =
    insertMultiple(records.toList())

fun TransactionParticipantModelMapper.insertSelective(record: TransactionParticipantModelRecord) =
    insert(this::insert, record, TransactionParticipantModel) {
        map(transactionId).toPropertyWhenPresent("transactionId", record::transactionId)
        map(ackHostAndPort).toPropertyWhenPresent("ackHostAndPort", record::ackHostAndPort)
        map(applicationName).toPropertyWhenPresent("applicationName", record::applicationName)
        map(parentTransactionId).toPropertyWhenPresent("parentTransactionId", record::parentTransactionId)
        map(timeCreated).toPropertyWhenPresent("timeCreated", record::timeCreated)
        map(timeExpired).toPropertyWhenPresent("timeExpired", record::timeExpired)
        map(timeInserted).toPropertyWhenPresent("timeInserted", record::timeInserted)
        map(transactionType).toPropertyWhenPresent("transactionType", record::transactionType)
        map(version).toPropertyWhenPresent("version", record::version)
        map(states).toPropertyWhenPresent("states", record::states)
    }

private val columnList = listOf(transactionId, ackHostAndPort, applicationName, parentTransactionId, timeCreated, timeExpired, timeInserted, transactionType, version, states)

fun TransactionParticipantModelMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, TransactionParticipantModel, completer)

fun TransactionParticipantModelMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, TransactionParticipantModel, completer)

fun TransactionParticipantModelMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, TransactionParticipantModel, completer)

fun TransactionParticipantModelMapper.selectByPrimaryKey(transactionId_: Long) =
    selectOne {
        where(transactionId, isEqualTo(transactionId_))
    }

fun TransactionParticipantModelMapper.update(completer: UpdateCompleter) =
    update(this::update, TransactionParticipantModel, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: TransactionParticipantModelRecord) =
    apply {
        set(transactionId).equalTo(record::transactionId)
        set(ackHostAndPort).equalTo(record::ackHostAndPort)
        set(applicationName).equalTo(record::applicationName)
        set(parentTransactionId).equalTo(record::parentTransactionId)
        set(timeCreated).equalTo(record::timeCreated)
        set(timeExpired).equalTo(record::timeExpired)
        set(timeInserted).equalTo(record::timeInserted)
        set(transactionType).equalTo(record::transactionType)
        set(version).equalTo(record::version)
        set(states).equalTo(record::states)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: TransactionParticipantModelRecord) =
    apply {
        set(transactionId).equalToWhenPresent(record::transactionId)
        set(ackHostAndPort).equalToWhenPresent(record::ackHostAndPort)
        set(applicationName).equalToWhenPresent(record::applicationName)
        set(parentTransactionId).equalToWhenPresent(record::parentTransactionId)
        set(timeCreated).equalToWhenPresent(record::timeCreated)
        set(timeExpired).equalToWhenPresent(record::timeExpired)
        set(timeInserted).equalToWhenPresent(record::timeInserted)
        set(transactionType).equalToWhenPresent(record::transactionType)
        set(version).equalToWhenPresent(record::version)
        set(states).equalToWhenPresent(record::states)
    }

fun TransactionParticipantModelMapper.updateByPrimaryKey(record: TransactionParticipantModelRecord) =
    update {
        set(ackHostAndPort).equalTo(record::ackHostAndPort)
        set(applicationName).equalTo(record::applicationName)
        set(parentTransactionId).equalTo(record::parentTransactionId)
        set(timeCreated).equalTo(record::timeCreated)
        set(timeExpired).equalTo(record::timeExpired)
        set(timeInserted).equalTo(record::timeInserted)
        set(transactionType).equalTo(record::transactionType)
        set(version).equalTo(record::version)
        set(states).equalTo(record::states)
        where(transactionId, isEqualTo(record::transactionId))
    }

fun TransactionParticipantModelMapper.updateByPrimaryKeySelective(record: TransactionParticipantModelRecord) =
    update {
        set(ackHostAndPort).equalToWhenPresent(record::ackHostAndPort)
        set(applicationName).equalToWhenPresent(record::applicationName)
        set(parentTransactionId).equalToWhenPresent(record::parentTransactionId)
        set(timeCreated).equalToWhenPresent(record::timeCreated)
        set(timeExpired).equalToWhenPresent(record::timeExpired)
        set(timeInserted).equalToWhenPresent(record::timeInserted)
        set(transactionType).equalToWhenPresent(record::transactionType)
        set(version).equalToWhenPresent(record::version)
        set(states).equalToWhenPresent(record::states)
        where(transactionId, isEqualTo(record::transactionId))
    }