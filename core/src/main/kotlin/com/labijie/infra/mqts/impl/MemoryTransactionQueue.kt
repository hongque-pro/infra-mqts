package com.labijie.infra.mqts.impl

import com.labijie.infra.mqts.MQTransaction
import com.labijie.infra.mqts.abstractions.ITransactionHolder
import com.labijie.infra.mqts.abstractions.ITransactionQueue
import com.labijie.infra.mqts.scopeWith
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class MemoryTransactionQueue(private val transactionHolder: ITransactionHolder) : ITransactionQueue {

    private val blockingQueue: BlockingQueue<QueueItem> = LinkedBlockingQueue()
    private val handlers: MutableMap<String, (transaction: MQTransaction) -> Unit> = mutableMapOf()

    init {
        this.start()
    }

    override fun send(queue: String, transaction: MQTransaction) {
        this.blockingQueue.put(QueueItem(transaction, queue, transaction.transactionType))
    }

    override fun registerHandler(queue: String, transactionType: String, handler: (transaction: MQTransaction) -> Unit) {
        handlers.put("$queue:$transactionType", handler)
    }

    fun start() {
        thread {
            while (true) {
                val item = blockingQueue.poll()
                if (item != null) {
                    this.transactionHolder.scopeWith(item.transaction).use {
                        handlers.forEach {
                            if (it.key == item.getKey()) {
                                it.value.invoke(item.transaction)
                            }
                        }
                    }
                }
            }
        }
    }

    data class QueueItem(public val transaction: MQTransaction, private val queue: String, private val type: String) {
        fun getKey(): String {
            return "$queue:$type"
        }
    }
}