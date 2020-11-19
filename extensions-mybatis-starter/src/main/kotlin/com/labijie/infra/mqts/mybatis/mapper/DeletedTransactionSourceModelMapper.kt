/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.labijie.infra.mqts.mybatis.mapper

import com.labijie.infra.mqts.mybatis.domain.DeletedTransactionSourceModelRecord
import org.apache.ibatis.annotations.DeleteProvider
import org.apache.ibatis.annotations.InsertProvider
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.annotations.UpdateProvider
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

@Mapper
interface DeletedTransactionSourceModelMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    fun insert(insertStatement: InsertStatementProvider<DeletedTransactionSourceModelRecord>): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insertMultiple")
    fun insertMultiple(multipleInsertStatement: MultiRowInsertStatementProvider<DeletedTransactionSourceModelRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("DeletedTransactionSourceModelRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): DeletedTransactionSourceModelRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="DeletedTransactionSourceModelRecordResult", value = [
        Result(column="transaction_id", property="transactionId", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="ack_host_and_port", property="ackHostAndPort", jdbcType=JdbcType.VARCHAR),
        Result(column="application_name", property="applicationName", jdbcType=JdbcType.VARCHAR),
        Result(column="parent_transaction_id", property="parentTransactionId", jdbcType=JdbcType.BIGINT),
        Result(column="time_created", property="timeCreated", jdbcType=JdbcType.BIGINT),
        Result(column="time_expired", property="timeExpired", jdbcType=JdbcType.BIGINT),
        Result(column="transaction_type", property="transactionType", jdbcType=JdbcType.VARCHAR),
        Result(column="version", property="version", jdbcType=JdbcType.VARCHAR),
        Result(column="data", property="data", jdbcType=JdbcType.LONGVARBINARY),
        Result(column="states", property="states", jdbcType=JdbcType.LONGVARCHAR)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<DeletedTransactionSourceModelRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}