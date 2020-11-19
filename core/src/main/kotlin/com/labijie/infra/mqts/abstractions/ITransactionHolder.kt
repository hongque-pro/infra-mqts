package com.labijie.infra.mqts.abstractions

import com.labijie.infra.mqts.MQTransaction

interface ITransactionHolder {
    var currentTransaction : MQTransaction?
}