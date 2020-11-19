package com.labijie.infra.mqts.discovery.web

import com.labijie.infra.mqts.discovery.web.Constants.AckWebPath
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-09
 */
interface IAckEndpoint {
    @PostMapping(AckWebPath)
    fun sendAck(@RequestBody ackModel: AckModel): AckResponse
}