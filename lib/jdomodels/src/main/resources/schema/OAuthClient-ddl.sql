CREATE TABLE IF NOT EXISTS `OAUTH_CLIENT` (
  `ID` bigint(20) NOT NULL,
  `NAME` varchar(256) NOT NULL UNIQUE,
  `SECRET_HASH` char(74),
  `OAUTH_SECTOR_IDENTIFIER_URI` varchar(256) NOT NULL,
  `IS_VERIFIED` boolean NOT NULL,
  `PROPERTIES` mediumblob,
  `CREATED_BY` bigint(20) NOT NULL,
  `CREATED_ON` bigint(20) NOT NULL,
  `MODIFIED_ON` bigint(20) NOT NULL,
  `ETAG` char(36) NOT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `OAUTH_CLIENT_SECTOR_IDENTIFIER_FK` FOREIGN KEY (`OAUTH_SECTOR_IDENTIFIER_URI`) REFERENCES `OAUTH_SECTOR_IDENTIFIER` (`URI`),
  CONSTRAINT `OAUTH_CLIENT_CREATED_BY_FK` FOREIGN KEY (`CREATED_BY`) REFERENCES `JDOUSERGROUP` (`ID`)
)


	
	