/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.labijie.infra.mqts.mybatis.domain

data class TransactionParticipantModelRecord(
    var transactionId: Long? = null,
    var ackHostAndPort: String? = null,
    var applicationName: String? = null,
    var parentTransactionId: Long? = null,
    var timeCreated: Long? = null,
    var timeExpired: Long? = null,
    var timeInserted: Long? = null,
    var transactionType: String? = null,
    var version: String? = null,
    var states: String? = null
)