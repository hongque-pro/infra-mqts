package com.labijie.infra.mqts.configuration


class QueueConfig(var server: String = "") {
    var properties: HashMap<String, Any> = HashMap()
}