package com.labijie.infra.mqts

enum class TransactionResult(private val value:Int) {
    Committed(0),
    Canceled(1);

    fun getValue(): Int {
        return this.value
    }
}