/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.labijie.infra.mqts.mybatis.mapper

import java.sql.JDBCType
import org.mybatis.dynamic.sql.SqlTable

object TransactionSourceExpiredModelDynamicSqlSupport {
    object TransactionSourceExpiredModel : SqlTable("`mqts_expired`") {
        val transactionId = column<Long>("`transaction_id`", JDBCType.BIGINT)

        val ackHostAndPort = column<String>("`ack_host_and_port`", JDBCType.VARCHAR)

        val applicationName = column<String>("`application_name`", JDBCType.VARCHAR)

        val parentTransactionId = column<Long>("`parent_transaction_id`", JDBCType.BIGINT)

        val timeCreated = column<Long>("`time_created`", JDBCType.BIGINT)

        val timeExpired = column<Long>("`time_expired`", JDBCType.BIGINT)

        val transactionType = column<String>("`transaction_type`", JDBCType.VARCHAR)

        val version = column<String>("`version`", JDBCType.VARCHAR)

        val data = column<String>("`data`", JDBCType.LONGVARCHAR)

        val states = column<String>("`states`", JDBCType.LONGVARCHAR)
    }
}