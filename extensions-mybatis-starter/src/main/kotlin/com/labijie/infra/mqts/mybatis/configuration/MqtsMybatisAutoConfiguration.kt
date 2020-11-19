package com.labijie.infra.mqts.mybatis.configuration

import com.labijie.infra.mqts.mybatis.MybatisRepository
import com.labijie.infra.mqts.spring.condition.ConditionalOnMqts
import com.labijie.infra.mqts.spring.configuration.MqtsAutoConfiguration
import com.labijie.infra.mqts.spring.configuration.MqtsOptionalAutoConfiguration
import com.labijie.infra.spring.configuration.getApplicationName
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.transaction.support.TransactionTemplate

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-12-26
 */
@ConditionalOnMqts
@Configuration
@AutoConfigureAfter(MqtsAutoConfiguration::class)
@AutoConfigureBefore(MqtsOptionalAutoConfiguration::class)
@MapperScan("com.labijie.infra.mqts.mybatis.mapper")
class MqtsMybatisAutoConfiguration {

    @ConditionalOnClass(SqlSessionFactory::class)
    @Bean
    fun mybatisRepository(environment: Environment, transactionTemplate: TransactionTemplate): MybatisRepository{
        return MybatisRepository(environment.getApplicationName(), transactionTemplate)
    }
}