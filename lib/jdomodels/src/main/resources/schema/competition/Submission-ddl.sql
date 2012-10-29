CREATE TABLE JDOSUBMISSION (
    ID bigint(20) NOT NULL,
    NAME varchar(256) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
    COMPETITION_ID bigint(20) NOT NULL,
    USER_ID bigint(20) NOT NULL,
    ENTITY_ID bigint(20) NOT NULL,    
    CREATED_ON datetime NOT NULL,
    STATUS int NOT NULL,
    SCORE bigint(20) DEFAULT NULL,
    PRIMARY KEY (ID),
    FOREIGN KEY (COMPETITION_ID) REFERENCES JDOCOMPETITION (ID),
    FOREIGN KEY (USER_ID) REFERENCES JDOUSERGROUP (ID),
    FOREIGN KEY (ENTITY_ID) REFERENCES JDONODE (ID)
);