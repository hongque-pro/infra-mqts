package com.labijie.infra.mqts.kafka

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-22
 */
class Constants {
    companion object {
        val TRANSACTION_TOPIC_PREFIX = "mqts-"
        val ACK_TOPIC_STUFFIX = "-ack"
        val REDO_TOPIC_STUFFIX = "-redo"

        val PARTICIPANT_POOL_SIZE = "participantPoolSize"
        val ACK_POOL_SIZE = "ackPoolSize"
    }
}