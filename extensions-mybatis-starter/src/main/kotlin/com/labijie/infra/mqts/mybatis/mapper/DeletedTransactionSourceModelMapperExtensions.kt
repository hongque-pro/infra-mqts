/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.labijie.infra.mqts.mybatis.mapper

import com.labijie.infra.mqts.mybatis.domain.DeletedTransactionSourceModelRecord
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.ackHostAndPort
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.applicationName
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.data
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.parentTransactionId
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.states
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.timeCreated
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.timeExpired
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.transactionId
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.transactionType
import com.labijie.infra.mqts.mybatis.mapper.DeletedTransactionSourceModelDynamicSqlSupport.DeletedTransactionSourceModel.version
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun DeletedTransactionSourceModelMapper.count(completer: CountCompleter) =
    countFrom(this::count, DeletedTransactionSourceModel, completer)

fun DeletedTransactionSourceModelMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, DeletedTransactionSourceModel, completer)

fun DeletedTransactionSourceModelMapper.deleteByPrimaryKey(transactionId_: Long) =
    delete {
        where(transactionId, isEqualTo(transactionId_))
    }

fun DeletedTransactionSourceModelMapper.insert(record: DeletedTransactionSourceModelRecord) =
    insert(this::insert, record, DeletedTransactionSourceModel) {
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

fun DeletedTransactionSourceModelMapper.insertMultiple(records: Collection<DeletedTransactionSourceModelRecord>) =
    insertMultiple(this::insertMultiple, records, DeletedTransactionSourceModel) {
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

fun DeletedTransactionSourceModelMapper.insertMultiple(vararg records: DeletedTransactionSourceModelRecord) =
    insertMultiple(records.toList())

fun DeletedTransactionSourceModelMapper.insertSelective(record: DeletedTransactionSourceModelRecord) =
    insert(this::insert, record, DeletedTransactionSourceModel) {
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

fun DeletedTransactionSourceModelMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, DeletedTransactionSourceModel, completer)

fun DeletedTransactionSourceModelMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, DeletedTransactionSourceModel, completer)

fun DeletedTransactionSourceModelMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, DeletedTransactionSourceModel, completer)

fun DeletedTransactionSourceModelMapper.selectByPrimaryKey(transactionId_: Long) =
    selectOne {
        where(transactionId, isEqualTo(transactionId_))
    }

fun DeletedTransactionSourceModelMapper.update(completer: UpdateCompleter) =
    update(this::update, DeletedTransactionSourceModel, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: DeletedTransactionSourceModelRecord) =
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

fun KotlinUpdateBuilder.updateSelectiveColumns(record: DeletedTransactionSourceModelRecord) =
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

fun DeletedTransactionSourceModelMapper.updateByPrimaryKey(record: DeletedTransactionSourceModelRecord) =
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

fun DeletedTransactionSourceModelMapper.updateByPrimaryKeySelective(record: DeletedTransactionSourceModelRecord) =
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