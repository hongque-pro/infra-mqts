
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
