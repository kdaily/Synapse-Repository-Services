CREATE TABLE IF NOT EXISTS `SES_NOTIFICATIONS` (
  `ID` BIGINT(20) NOT NULL,
  `CREATED_ON` TIMESTAMP(3) NOT NULL,
  `INSTANCE_NUMBER` INT NOT NULL,
  `SES_MESSAGE_ID` VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `SES_FEEDBACK_ID` VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `NOTIFICATION_TYPE` ENUM('BOUNCE', 'COMPLAINT', 'DELIVERY', 'UNKNOWN') NOT NULL,
  `NOTIFICATION_SUBTYPE` VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `NOTIFICATION_REASON` VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `NOTIFICATION_BODY` JSON NOT NULL,
  PRIMARY KEY (`ID`),
  INDEX `SES_NOTIFICATIONS_CREATED_ON_INDEX` (`CREATED_ON`),
  INDEX `SES_NOTIFICATIONS_SES_MESSAGE_ID_INDEX` (`SES_MESSAGE_ID`)
)
