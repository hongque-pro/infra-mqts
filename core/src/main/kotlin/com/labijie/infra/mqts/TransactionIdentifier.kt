package com.labijie.infra.mqts

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-05
 */
data class TransactionIdentifier(val clazz:KClass<*>, val method:KFunction<*>)