package com.labijie.infra.mqts

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class TransactionParticipantAttribute(val info: IParticipantInfo,
                                           val declareClass: KClass<out Any>,
                                           val method: KFunction<*>)