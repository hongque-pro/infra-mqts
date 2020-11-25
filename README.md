# MQTS
**MQ Transaction service**

基于可靠消息的分布式柔性事务开发包（类似 SAGA\TCC， 但是比 SAGA\TCC 更简单），对业务提供异步强一致性（最终一致性）能力

![maven central version](https://img.shields.io/maven-central/v/com.labijie.infra/mqts-core?style=flat-square)
![workflow status](https://img.shields.io/github/workflow/status/hongque-pro/infra-mqts/Gradle%20Build%20And%20Release?label=CI%20publish&style=flat-square)
![license](https://img.shields.io/github/license/hongque-pro/infra-mqts?style=flat-square)

## MQTS 对比 SAGA 类柔性事务，优势、缺点：
优点：

1. 无需分布式事务协调器，只依赖 MQ，部署简单
1. 全程异步化，对事务传播零知识
1. 无需编写回滚、冲正代码，逻辑简单可靠
1. 因为没有回滚动作，不会引入隔离性、悬挂等问题


缺点：

1. 只支持正向事务，即事务无法回滚（业务正确的情况下通过手动重放事务可以应对大多数要求一致性的场景）
1. 事务无法取消，一旦业务不正确只能手工冲正


如果需具有回滚能力的分布式事务，推荐使用 [SEATA](https://github.com/seata/seata)


## 简单描述
1. 系统 A 处理任务
1. 处理成功后发送消息给消息中间件
1. 消息投递给系统 B
1. 系统 B 处理任务
1. 系统 B 处理任务后通过MQ或其他传输方式（支持微服务发现调用）发送应答消息给系统 A
1. 系统 A 收到应答消息提交事务(提交事务代码也可以包含本地数据库事务操作)

## 追踪链路支持（基于 infra-telemetry）

以下为将追踪信息收集到 elasticsearch apm 示例：
> 追踪甘特图可以帮助你更好的理解完整 MQTS 事务流程

![alt text](https://github.com/endink/endink/blob/master/infra-tracing.PNG?raw=true)

> 自动处理重试、超时死信队列、事务重放等问题

## 使用方法

**引入依赖（Gradle)**

```groovy
    compile "com.labijie.infra:mqts-all-starter:$infra_mqts_version"
```
**应用程序入口处使用注解**
```kotlin
@SpringBootApplication
@EnableMqts //开启 mqts 
class Application
```

**配置消息队列中间件（默认使用 kafka, 可以通过实现 ITransactionQueue 支持其他中间件）**

```yaml
infra:
  telemetry:
    tracing:
      exporter: kafka
      exporter-properties:
        bootstrap.servers : 192.168.199.238:9092 //默认开启了追踪（可以展现多链路事务甘特图）
  mqts:
    queues:
      dummy: //队列名称
        server: 192.168.199.238:9092 //kafka 地址
```

**事务源（流程描述中的系统 A）**

```kotlin
@MQTransactionSource("your-transaction-name", queue = "dummy", retryIntervalSeconds = [5, 5, 10, 30, 120])
fun startSource(): ReturnType {
    return ReturnType("Transaction has been sent.")
}
```
> 注解中的 **retryIntervalSeconds** 属性表示事务重试间隔： 5 秒，5 秒，10秒， 30秒，120 秒, 120 秒... 直到超时
> **queue** 表示队列名称，和配置中的队列保持一致（支持使用多个消息队列中间件） 

**事务参与者（流程描述中的系统 B）**
```kotlin
@MQTransactionParticipant("your-transaction-name", queue = "dummy")
fun joinTransaction(transactionData: ReturnType){
    logger.info("Transaction was received!")
}
```

> **注意**：例子中 ReturnType, 事务源的输出作为参与者的输入，简单起见，消息发送和本地事务通过数据库事务保持强一致性，数据库依赖如下表结构（mysql）:

```text

--
-- Table structure for table `mqts_expired`
--

DROP TABLE IF EXISTS `mqts_expired`;
CREATE TABLE `mqts_expired` (
  `transaction_id` bigint(20) NOT NULL,
  `ack_host_and_port` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `application_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data` mediumtext COLLATE utf8mb4_unicode_ci,
  `parent_transaction_id` bigint(20) DEFAULT NULL,
  `states` mediumtext COLLATE utf8mb4_unicode_ci,
  `time_created` bigint(20) NOT NULL,
  `time_expired` bigint(20) NOT NULL,
  `transaction_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `IDXb7e52gm2dd55qn3bgpdif9bd7` (`time_created`),
  KEY `IDXjlr1dc40jufjn307hpxswosg4` (`time_expired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `mqts_participants`
--

DROP TABLE IF EXISTS `mqts_participants`;
CREATE TABLE `mqts_participants` (
  `transaction_id` bigint(20) NOT NULL,
  `ack_host_and_port` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `application_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parent_transaction_id` bigint(20) DEFAULT NULL,
  `states` mediumtext COLLATE utf8mb4_unicode_ci,
  `time_created` bigint(20) NOT NULL,
  `time_expired` bigint(20) NOT NULL,
  `time_inserted` bigint(20) NOT NULL,
  `transaction_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `IDX617cgiuu33p47jg0gocxugqja` (`time_created`),
  KEY `IDXxe2ydrah05g142uhfk70xbyb` (`time_expired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `mqts_sources`
--

DROP TABLE IF EXISTS `mqts_sources`;
CREATE TABLE `mqts_sources` (
  `transaction_id` bigint(20) NOT NULL,
  `ack_host_and_port` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `application_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data` mediumblob,
  `parent_transaction_id` bigint(20) DEFAULT NULL,
  `states` mediumtext COLLATE utf8mb4_unicode_ci,
  `time_created` bigint(20) NOT NULL,
  `time_expired` bigint(20) NOT NULL,
  `transaction_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `IDXfjs9u3m9g3w0savurx6p2elea` (`time_created`),
  KEY `IDXni9yf7ssjo4tnc8npua9kd971` (`time_expired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dump completed on 2019-02-25 15:15:06


DROP TABLE IF EXISTS `mqts_sources_deleted`;
CREATE TABLE `mqts_sources_deleted` (
  `transaction_id` bigint(20) NOT NULL,
  `ack_host_and_port` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `application_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data` mediumblob,
  `parent_transaction_id` bigint(20) DEFAULT NULL,
  `states` mediumtext COLLATE utf8mb4_unicode_ci,
  `time_created` bigint(20) NOT NULL,
  `time_expired` bigint(20) NOT NULL,
  `transaction_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `IDXfjs9u3m9g3w0savurx6p2elea` (`time_created`),
  KEY `IDXni9yf7ssjo4tnc8npua9kd971` (`time_expired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

```
