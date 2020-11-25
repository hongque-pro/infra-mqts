package com.labijie.infra.mqts.dummy

import com.labijie.infra.mqts.MQTransactionParticipant
import com.labijie.infra.mqts.MQTransactionSource
import com.labijie.infra.mqts.spring.annotation.EnableMqts
import com.labijie.infra.utils.logger
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@RestController
@SpringBootApplication
@EnableMqts
class Application{

    @MQTransactionSource("dummy-tran", queue = "dummy", retryIntervalSeconds = [2, 5, 10], timeoutSeconds = 60)
    @GetMapping("/mqts")
    fun startSource(): String {
        return "Transaction has been sent."
    }

    @MQTransactionParticipant("dummy-tran", queue = "dummy")
    fun receivedTran(transactionData: String){
        logger.info("Transaction was received!")
        throw RuntimeException("This is a dummy error!")
    }
}

fun main(){
    SpringApplication.run(Application::class.java)
}