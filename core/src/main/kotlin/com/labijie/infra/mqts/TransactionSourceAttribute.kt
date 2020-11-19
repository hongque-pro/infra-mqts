package com.labijie.infra.mqts

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class TransactionSourceAttribute(
    val annotation: ISourceInfo,
    val declareClass: KClass<*>,
    val method: KFunction<*>) {


  fun configEquals(other: TransactionSourceAttribute): Boolean {
    if (this === other) {
      return true
    }
    return (other.annotation.type == this.annotation.type &&
        other.method == this.method &&
        other.annotation.queue == this.annotation.queue &&
        other.annotation.timeoutSeconds == this.annotation.timeoutSeconds &&
        other.annotation.retryIntervalSeconds contentEquals this.annotation.retryIntervalSeconds)

  }

}