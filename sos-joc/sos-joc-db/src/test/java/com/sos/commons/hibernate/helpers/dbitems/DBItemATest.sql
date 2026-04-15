######################################################################################################
# MySQL
######################################################################################################
set SQL_MODE=ANSI_QUOTES;
CREATE TABLE IF NOT EXISTS A_TEST(
    "ID"                        	BIGINT(20)      UNSIGNED    NOT NULL    AUTO_INCREMENT,
    "NAME"                      	VARCHAR(255)                NOT NULL, 
    "JAVA_DATE_MANUAL"          	DATETIME                    NOT NULL,
    "JAVA_DATE_AUTO"            	DATETIME                    NOT NULL,
    "DB_CURRENT_TIMESTAMP_AUTO" 	DATETIME                    NOT NULL,
    "DB_CURRENT_UTC_TIMESTAMP_AUTO"	DATETIME                    NOT NULL,
    PRIMARY KEY ("ID")
) ENGINE=InnoDB;



######################################################################################################
# ORACLE
######################################################################################################
DECLARE table_exist number;
BEGIN
SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='A_TEST';
    IF (table_exist = 0) THEN
        EXECUTE IMMEDIATE   'CREATE TABLE A_TEST (
                                "ID"                        N	UMBER(19)       NOT NULL,
                                "NAME"                      	NVARCHAR2(255)  NOT NULL, 
                                "JAVA_DATE_MANUAL"          	DATE            NOT NULL,
                                "JAVA_DATE_AUTO"            	DATE            NOT NULL,
                                "DB_CURRENT_TIMESTAMP_AUTO" 	DATE            NOT NULL,
								"DB_CURRENT_UTC_TIMESTAMP_AUTO"	DATE       		NOT NULL,
                                PRIMARY KEY ("ID")
                            )';
        DECLARE sequence_exist number; 
        BEGIN 
            SELECT COUNT(*) INTO sequence_exist FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SEQ_A_TEST'; 
            IF (sequence_exist = 0) THEN 
                EXECUTE IMMEDIATE   'CREATE SEQUENCE SEQ_A_TEST
                                        INCREMENT BY 1
                                        START WITH 1
                                        MAXVALUE 9999999999999999999
                                        MINVALUE 1 CYCLE';          
            END IF; 
        END;
    END IF; 
END;
