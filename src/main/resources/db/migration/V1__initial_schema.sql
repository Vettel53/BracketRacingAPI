--
-- Table structure for table `app_user`
--

DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hashed_password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `run`
--

DROP TABLE IF EXISTS `run`;
CREATE TABLE `run` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `car` varchar(255) NOT NULL,
  `date` date NOT NULL,
  `dial` decimal(6,4) NOT NULL,
  `driver` varchar(255) NOT NULL,
  `full_track` decimal(6,4) NOT NULL,
  `half_track` decimal(6,4) NOT NULL,
  `lane` varchar(255) NOT NULL,
  `reaction` decimal(6,4) NOT NULL,
  `sixty_foot` decimal(6,4) NOT NULL,
  `speed` decimal(7,4) NOT NULL,
  `time` time(6) NOT NULL,
  `track` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_run_user_id` (`user_id`),
  CONSTRAINT `fk_run_user_id` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `vehicle`
--

DROP TABLE IF EXISTS `vehicle`;
CREATE TABLE `vehicle` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `weather`
--

DROP TABLE IF EXISTS `weather`;
CREATE TABLE `weather` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `air_density_no_water_vapor` decimal(38,2) DEFAULT NULL,
  `air_density_with_water_vapor` decimal(38,2) DEFAULT NULL,
  `corrected_barometer` decimal(38,2) DEFAULT NULL,
  `density_altitude` decimal(38,2) DEFAULT NULL,
  `dew_point` decimal(38,2) DEFAULT NULL,
  `grains` decimal(38,2) DEFAULT NULL,
  `relative_humidity` decimal(38,2) DEFAULT NULL,
  `saturation_pressure` decimal(38,2) DEFAULT NULL,
  `temperature` decimal(38,2) DEFAULT NULL,
  `uncorrected_barometer` decimal(38,2) DEFAULT NULL,
  `vapor_pressure` decimal(38,2) DEFAULT NULL,
  `wind_direction` decimal(38,2) DEFAULT NULL,
  `wind_speed` decimal(38,2) DEFAULT NULL,
  `run_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_weather_run_id` (`run_id`),
  CONSTRAINT `fk_weather_run_id` FOREIGN KEY (`run_id`) REFERENCES `run` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;