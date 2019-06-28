/*
 | JobScheduler history Interface tables for Oracle
*/
/* Table for SOS_JS_VARIABLES */
DECLARE table_exist number;
BEGIN 
    SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_VARIABLES';
    IF (table_exist = 0) THEN 
        EXECUTE IMMEDIATE   'CREATE TABLE SOS_JS_VARIABLES(
								"NAME"              VARCHAR2(255) 	NOT NULL,             
								"NUMERIC_VALUE"     NUMBER(10) ,                
								"TEXT_VALUE"        VARCHAR2(255),
								"LOCK_VERSION"      NUMBER(10)     	DEFAULT 0 	NOT NULL,    
								PRIMARY KEY ("NAME")
							)';  
    END IF;
END;
/    

/* Table for SOS_JS_HISTORY_MASTERS */
DECLARE table_exist number;
BEGIN 
    SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_HISTORY_MASTERS';
    IF (table_exist = 0) THEN 
        EXECUTE IMMEDIATE   'CREATE TABLE SOS_JS_HISTORY_MASTERS(
								"ID"                        NUMBER(10)               	NOT NULL,
								"MASTER_ID"                 VARCHAR2(100)    			NOT NULL,
								"HOSTNAME"          		VARCHAR2(100)  				NOT NULL,
								"PORT"              		NUMBER(10)     				NOT NULL,
    							"TIMEZONE"                  VARCHAR2(100)    			NOT NULL,
								"START_TIME"                DATE        			    NOT NULL,
								"PRIMARY_MASTER"            NUMBER(1)                   NOT NULL,
                                "EVENT_ID"           	    CHAR(16)					NOT NULL,
								"CREATED"                   DATE        		    	NOT NULL,
								CONSTRAINT SOS_JS_HM_UNIQUE UNIQUE ("EVENT_ID"), 
								PRIMARY KEY ("ID")
							)';  
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HM_INX_MID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HM_INX_MID ON SOS_JS_HISTORY_MASTERS("MASTER_ID")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HM_INX_TZ';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HM_INX_TZ ON SOS_JS_HISTORY_MASTERS("TIMEZONE")';
            END IF;
        END;
		DECLARE sequence_exist number; 
        BEGIN 
            SELECT COUNT(*) INTO sequence_exist FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_HM_SEQ'; 
            IF (sequence_exist = 0) THEN 
                EXECUTE IMMEDIATE   'CREATE SEQUENCE SOS_JS_HM_SEQ';          
            END IF; 
        END;
    END IF;
END;
/

/* Table for SOS_JS_HISTORY_AGENTS */
DECLARE table_exist number;
BEGIN 
    SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_HISTORY_AGENTS';
    IF (table_exist = 0) THEN 
        EXECUTE IMMEDIATE   'CREATE TABLE SOS_JS_HISTORY_AGENTS(
								"ID"                        NUMBER(10)               	NOT NULL,
								"MASTER_ID"                 VARCHAR2(100)    			NOT NULL,  /* SOS_JS_HISTORY_MASTERS.MASTER_ID */
							    "PATH"                      VARCHAR2(100)    			NOT NULL,
								"URI"                       VARCHAR2(100)    			NOT NULL,
								"TIMEZONE"                  VARCHAR2(100)    			NOT NULL,
								"START_TIME"                DATE        			    NOT NULL,
								"EVENT_ID"           	    CHAR(16)					NOT NULL,
								"CREATED"                   DATE        		    	NOT NULL,
								CONSTRAINT SOS_JS_HA_UNIQUE UNIQUE ("EVENT_ID"), 
								PRIMARY KEY ("ID")
							)';  
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HA_INX_MIDP';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HA_INX_MIDP ON SOS_JS_HISTORY_AGENTS("MASTER_ID","PATH")';
            END IF;
        END;
		DECLARE sequence_exist number; 
        BEGIN 
            SELECT COUNT(*) INTO sequence_exist FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_HA_SEQ'; 
            IF (sequence_exist = 0) THEN 
                EXECUTE IMMEDIATE   'CREATE SEQUENCE SOS_JS_HA_SEQ';          
            END IF; 
        END;
    END IF;
END;
/

/* Table for SOS_JS_HISTORY_ORDERS */
DECLARE table_exist number;
BEGIN 
    SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_HISTORY_ORDERS';
    IF (table_exist = 0) THEN 
        EXECUTE IMMEDIATE   'CREATE TABLE SOS_JS_HISTORY_ORDERS(
								"ID"                        NUMBER(10)               	NOT NULL,
								"MASTER_ID"                 VARCHAR2(100)    			NOT NULL,
								"ORDER_KEY"                 VARCHAR2(255)    			NOT NULL,
								"WORKFLOW_PATH"             VARCHAR2(255)               NOT NULL,   
                                "WORKFLOW_VERSION_ID"       VARCHAR2(255)               NOT NULL,   /* #2019-06-13T08:43:29Z */
                                "WORKFLOW_POSITION"         VARCHAR2(255)    			NOT NULL, 	/* 1#fork_1#0 */
								"WORKFLOW_FOLDER"           VARCHAR2(255)               NOT NULL,
                                "WORKFLOW_NAME"             VARCHAR2(255)               NOT NULL,
                                "WORKFLOW_TITLE"            VARCHAR2(255),                          /* TODO */
                                "MAIN_PARENT_ID"            NUMBER(10)               	NOT NULL,	/* SOS_JS_HISTORY_ORDERS.ID of the main order */
								"PARENT_ID"                 NUMBER(10)              	NOT NULL,   /* SOS_JS_HISTORY_ORDERS.ID of the parent order */
								"PARENT_ORDER_KEY"          VARCHAR2(255)    			NOT NULL,   /* SOS_JS_HISTORY_ORDERS.ORDER_KEY */
								"HAS_CHILDREN"              NUMBER(1)                   NOT NULL,
                                "RETRY_COUNTER"             NUMBER(10)                  NOT NULL,
                                "NAME"                      VARCHAR2(255)    			NOT NULL,   /* TODO */
								"TITLE"                     VARCHAR2(255),               			/* TODO */
								"START_CAUSE"               VARCHAR2(50)     			NOT NULL,   /* implemented: unknown(period),fork. planned: file trigger, setback, unskip, unstop ... */
								"START_TIME_PLANNED"        DATE,                   			    /* NOT NULL ??? */
								"START_TIME"                DATE        			    NOT NULL,
								"START_WORKFLOW_POSITION"   VARCHAR2(255)    			NOT NULL,
								"START_EVENT_ID"           	CHAR(16)					NOT NULL,   /* OrderAdded eventId */
								"START_PARAMETERS"   		VARCHAR2(2000),							/* TODO length */	
								"CURRENT_ORDER_STEP_ID"		NUMBER(10)  				NOT NULL,	/* SOS_JS_HISTORY_ORDER_STEPS.ID */
								"END_TIME"                  DATE,
								"END_WORKFLOW_POSITION"     VARCHAR2(255),
								"END_EVENT_ID"           	CHAR(16),    							/* OrderFinisched eventId */
								"END_ORDER_STEP_ID"         NUMBER(10) 					NOT NULL,   /* SOS_JS_HISTORY_ORDER_STEPS.ID */
							    "STATUS"                    VARCHAR2(255)    			NOT NULL,   /* planned: planned, running, completed, cancelled, suspended... */
								"STATE_TEXT"                VARCHAR2(255),               			/* TODO */
								"ERROR"                     NUMBER(1)           		NOT NULL,   /* TODO */
								"ERROR_CODE"                VARCHAR2(50),                			/* TODO */
								"ERROR_TEXT"                VARCHAR2(255),               			/* TODO */
								"ERROR_ORDER_STEP_ID"       NUMBER(10) 					NOT NULL,   /* SOS_JS_HISTORY_ORDER_STEPS.ID */
				                "LOG_ID"                    NUMBER(10)                  NOT NULL,   /* SOS_JS_HISTORY_LOGS.ID */
                                "CONSTRAINT_HASH"	        CHAR(64)	               	NOT NULL,   /* hash from "MASTER_ID", "START_EVENT_ID"*/
								"CREATED"                   DATE        		    	NOT NULL,
								"MODIFIED"                  DATE        			    NOT NULL,
								CONSTRAINT SOS_JS_HO_UNIQUE UNIQUE ("CONSTRAINT_HASH"),              /* used by history*/  
								PRIMARY KEY ("ID")
							)';  
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HO_INX_MID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HO_INX_MID ON SOS_JS_HISTORY_ORDERS("MASTER_ID")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HO_INX_OK';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HO_INX_OK ON SOS_JS_HISTORY_ORDERS("ORDER_KEY")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HO_INX_COSID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HO_INX_COSID ON SOS_JS_HISTORY_ORDERS("CURRENT_ORDER_STEP_ID")';
            END IF;
        END;
		DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HO_INX_MPID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HO_INX_MPID ON SOS_JS_HISTORY_ORDERS("MAIN_PARENT_ID")';
            END IF;
        END;
		DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HO_INX_PID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HO_INX_PID ON SOS_JS_HISTORY_ORDERS("PARENT_ID")';
            END IF;
        END;
		DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HO_INX_STIME';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HO_INX_STIME ON SOS_JS_HISTORY_ORDERS("START_TIME")';
            END IF;
        END;
		DECLARE sequence_exist number; 
        BEGIN 
            SELECT COUNT(*) INTO sequence_exist FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_HO_SEQ'; 
            IF (sequence_exist = 0) THEN 
                EXECUTE IMMEDIATE   'CREATE SEQUENCE SOS_JS_HO_SEQ';          
            END IF; 
        END;
    END IF;
END;
/
		
/* Table for SOS_JS_HISTORY_ORDER_STEPS */
DECLARE table_exist number;
BEGIN 
    SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_HISTORY_ORDER_STEPS';
    IF (table_exist = 0) THEN 
        EXECUTE IMMEDIATE   'CREATE TABLE SOS_JS_HISTORY_ORDER_STEPS(
								"ID"                        NUMBER(10)              	NOT NULL,
								"MASTER_ID"                 VARCHAR2(100)    			NOT NULL,
								"ORDER_KEY"                 VARCHAR2(255)               NOT NULL,
                                "WORKFLOW_PATH"             VARCHAR2(255)               NOT NULL,           
                                "WORKFLOW_VERSION_ID"       VARCHAR2(255)               NOT NULL,   /* #2019-06-13T08:43:29Z */
                                "WORKFLOW_POSITION"         VARCHAR2(255)    			NOT NULL,	/* 1#fork_1#3 */
								"MAIN_ORDER_ID"             NUMBER(10)              	NOT NULL,		
								"ORDER_ID"                  NUMBER(10)              	NOT NULL,		
								"POSITION"                  NUMBER(10)                  NOT NULL,   /* 3 - last position from WORKFLOW_POSITION */
                                "RETRY_COUNTER"             NUMBER(10)                  NOT NULL,       
                                "JOB_NAME"                  VARCHAR2(255)    			NOT NULL,		
								"JOB_TITLE"                 VARCHAR2(255),               			/* TODO */
								"AGENT_PATH"                VARCHAR2(100)   		 	NOT NULL,	
								"AGENT_URI"                 VARCHAR2(100)   		 	NOT NULL,	
								"START_CAUSE"               VARCHAR2(50)     			NOT NULL,   /* planned: file trigger, setback, unskip, unstop ... */
								"START_TIME"                DATE        		    	NOT NULL,	
								"START_EVENT_ID"           	CHAR(16)					NOT NULL,   /* ProcessingStarted eventId */
								"START_PARAMETERS"   		VARCHAR2(2000),							/* TODO length */	
								"END_TIME"                  DATE,		
								"END_EVENT_ID"           	CHAR(16),    							/* Processed eventId */
								"END_PARAMETERS"   			VARCHAR2(2000),							/* TODO length */	
								"RETURN_CODE"               NUMBER(10) ,		
								"STATUS"                    VARCHAR2(255)    			NOT NULL,   /* planned: running, completed, stopped, skipped ... */
								"ERROR"                     NUMBER(1)           		NOT NULL,   /* TODO */
								"ERROR_CODE"                VARCHAR2(50),                			/* TODO */
								"ERROR_TEXT"                VARCHAR2(255),               			/* TODO */
								"LOG_ID"                    NUMBER(10)                  NOT NULL,   /* SOS_JS_HISTORY_LOGS.ID */
                                "CONSTRAINT_HASH"	        CHAR(64)        			NOT NULL,   /* hash from "MASTER_ID", "START_EVENT_ID"*/
								"CREATED"                   DATE        			    NOT NULL,
								"MODIFIED"                  DATE        	    		NOT NULL,
								CONSTRAINT SOS_JS_HOS_UNIQUE UNIQUE ("CONSTRAINT_HASH"), 			/* used by history*/
								PRIMARY KEY ("ID")
							)';  
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOS_INX_MID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOS_INX_MID ON SOS_JS_HISTORY_ORDER_STEPS("MASTER_ID")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOS_INX_OK';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOS_INX_OK ON SOS_JS_HISTORY_ORDER_STEPS("ORDER_KEY")';
            END IF;
        END;
		DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOS_INX_MOID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOS_INX_MOID ON SOS_JS_HISTORY_ORDER_STEPS("MAIN_ORDER_ID")';
            END IF;
        END;
		DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOS_INX_OID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOS_INX_OID ON SOS_JS_HISTORY_ORDER_STEPS("ORDER_ID")';
            END IF;
        END;
		DECLARE sequence_exist number; 
        BEGIN 
            SELECT COUNT(*) INTO sequence_exist FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_HOS_SEQ'; 
            IF (sequence_exist = 0) THEN 
                EXECUTE IMMEDIATE   'CREATE SEQUENCE SOS_JS_HOS_SEQ';          
            END IF; 
        END;
    END IF;
END;
/

/* Table for SOS_JS_HISTORY_ORDER_STATUS */
DECLARE table_exist number;
BEGIN 
    SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_HISTORY_ORDER_STATUS';
    IF (table_exist = 0) THEN 
        EXECUTE IMMEDIATE   'CREATE TABLE SOS_JS_HISTORY_ORDER_STATUS(
                                "ID"                        NUMBER(10)                  NOT NULL,
                                "MASTER_ID"                 VARCHAR2(100)               NOT NULL,
                                "ORDER_KEY"                 VARCHAR2(255)               NOT NULL,
                                "WORKFLOW_PATH"             VARCHAR2(255)               NOT NULL,           
                                "WORKFLOW_VERSION_ID"       VARCHAR2(255)               NOT NULL,   /* #2019-06-13T08:43:29Z */
                                "WORKFLOW_POSITION"         VARCHAR2(255)               NOT NULL,   /* 1#fork_1#3 */
                                "MAIN_ORDER_ID"             NUMBER(10)                  NOT NULL,       
                                "ORDER_ID"                  NUMBER(10)                  NOT NULL,       
                                "ORDER_STEP_ID"             NUMBER(10)                  NOT NULL,       
                                "STATUS"                    VARCHAR2(255)               NOT NULL,   /* started, cancelled, stopped, suspended, finished... */
                                "STATUS_TIME"               DATE                        NOT NULL,
                                "CONSTRAINT_HASH"           CHAR(64)                    NOT NULL,
                                "CREATED"                   DATE                        NOT NULL,
                                CONSTRAINT SOS_JS_HOST_UNIQUE UNIQUE ("CONSTRAINT_HASH"),           
                                PRIMARY KEY ("ID")
                            )';  
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOST_INX_MID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOST_INX_MID ON SOS_JS_HISTORY_ORDER_STATUS("MASTER_ID")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOST_INX_OK';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOST_INX_OK ON SOS_JS_HISTORY_ORDER_STATUS("ORDER_KEY")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOST_INX_MOID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOST_INX_MOID ON SOS_JS_HISTORY_ORDER_STATUS("MAIN_ORDER_ID")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HOST_INX_OID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HOST_INX_OID ON SOS_JS_HISTORY_ORDER_STATUS("ORDER_ID")';
            END IF;
        END;
        DECLARE sequence_exist number; 
        BEGIN 
            SELECT COUNT(*) INTO sequence_exist FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_HOST_SEQ'; 
            IF (sequence_exist = 0) THEN 
                EXECUTE IMMEDIATE   'CREATE SEQUENCE SOS_JS_HOST_SEQ';          
            END IF; 
        END;
    END IF;
END;
/

/* Table for SOS_JS_HISTORY_LOGS */
DECLARE table_exist number;
BEGIN 
    SELECT COUNT(*) INTO table_exist FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_HISTORY_LOGS';
    IF (table_exist = 0) THEN 
        EXECUTE IMMEDIATE   'CREATE TABLE SOS_JS_HISTORY_LOGS(
								"ID"                        NUMBER(10)              	NOT NULL,
								"MASTER_ID"                 VARCHAR2(100)    			NOT NULL,
								"MAIN_ORDER_ID"             NUMBER(10)                  NOT NULL,  /* SOS_JS_HISTORY_ORDERS.MAIN_PARENT_ID */
                                "ORDER_ID"                  NUMBER(10)                  NOT NULL,  /* SOS_JS_HISTORY_ORDERS.ID */
                                "ORDER_STEP_ID"             NUMBER(10)                  NOT NULL,  /* SOS_JS_HISTORY_ORDER_STEPS.ID */
                                "FILE_BASENAME"             VARCHAR2(255)    			NOT NULL,
								"FILE_SIZE_UNCOMPRESSED"    NUMBER(10)              	NOT NULL,
								"FILE_LINES_UNCOMPRESSED"   NUMBER(10)              	NOT NULL,
								"FILE_COMPRESSED"           BLOB              	        NOT NULL,
                                "CREATED"                   DATE                        NOT NULL,
								PRIMARY KEY ("ID")
							)';  
        
		DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HLOG_INX_MID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HLOG_INX_MID ON SOS_JS_HISTORY_LOGS("MASTER_ID")';
            END IF;
        END;
        DECLARE index_exist number;
        BEGIN
            SELECT COUNT(*) INTO index_exist FROM USER_INDEXES WHERE INDEX_NAME = 'SOS_JS_HLOG_INX_MOID';
            IF (index_exist = 0) THEN
                EXECUTE IMMEDIATE 'CREATE INDEX SOS_JS_HLOG_INX_MOID ON SOS_JS_HISTORY_LOGS("MAIN_ORDER_ID")';
            END IF;
        END;					
	    DECLARE sequence_exist number; 
        BEGIN 
            SELECT COUNT(*) INTO sequence_exist FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_HL_SEQ'; 
            IF (sequence_exist = 0) THEN 
                EXECUTE IMMEDIATE   'CREATE SEQUENCE SOS_JS_HL_SEQ';          
            END IF; 
        END;
    END IF;
END;
/

COMMIT;