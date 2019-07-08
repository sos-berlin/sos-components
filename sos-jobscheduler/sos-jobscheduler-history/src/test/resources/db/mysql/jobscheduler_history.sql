/*
 | JobScheduler history Interface tables for MySQL 5.0
*/
set SQL_MODE=ANSI_QUOTES;

/* Table for SOS_JS_SETTINGS */
CREATE TABLE IF NOT EXISTS SOS_JS_VARIABLES(
	"NAME"              VARCHAR(255)   						NOT NULL,             
    "NUMERIC_VALUE"     INT,                
    "TEXT_VALUE"        VARCHAR(255),
    "LOCK_VERSION"      INT            UNSIGNED	DEFAULT 0 	NOT NULL,    
    PRIMARY KEY ("NAME")
) ENGINE=MyISAM;

/* Table for SOS_JS_HISTORY_MASTERS */
CREATE TABLE IF NOT EXISTS SOS_JS_HISTORY_MASTERS(
	"ID"                        INT             UNSIGNED 	NOT NULL	AUTO_INCREMENT,
	"MASTER_ID"                 VARCHAR(100)    			NOT NULL,
	"URI"          		        VARCHAR(255)   				NOT NULL,
    "TIMEZONE"                 	VARCHAR(100)    			NOT NULL,
    "START_TIME"                DATETIME        			NOT NULL,
	"PRIMARY_MASTER"            TINYINT         UNSIGNED    NOT NULL,
    "EVENT_ID"           	    CHAR(16)					NOT NULL,  
	"CREATED"                   DATETIME        			NOT NULL,
	INDEX SOS_JS_HM_INX_MID("MASTER_ID"),
    INDEX SOS_JS_HM_INX_TZ("TIMEZONE"),	
    CONSTRAINT SOS_JS_HM_UNIQUE UNIQUE ("EVENT_ID"), 
	PRIMARY KEY ("ID")
) ENGINE=MyISAM;

/* Table for SOS_JS_HISTORY_AGENTS */
CREATE TABLE IF NOT EXISTS SOS_JS_HISTORY_AGENTS(
	"ID"                        INT             UNSIGNED 	NOT NULL	AUTO_INCREMENT,
	"MASTER_ID"                 VARCHAR(100)    			NOT NULL,  /* SOS_JS_HISTORY_MASTERS.MASTER_ID */
	"PATH"                      VARCHAR(100)    			NOT NULL,
	"URI"              			VARCHAR(100)    			NOT NULL,
    "TIMEZONE"                 	VARCHAR(100)    			NOT NULL,
    "START_TIME"                DATETIME        			NOT NULL,
	"EVENT_ID"           	    CHAR(16)					NOT NULL,
	"CREATED"                   DATETIME        			NOT NULL,
	INDEX SOS_JS_HA_INX_MIDP("MASTER_ID","PATH"),
 	CONSTRAINT SOS_JS_HA_UNIQUE UNIQUE ("EVENT_ID"), 	
	PRIMARY KEY ("ID")
) ENGINE=MyISAM;
	
/* Table for SOS_JS_HISTORY_ORDERS */
CREATE TABLE IF NOT EXISTS SOS_JS_HISTORY_ORDERS(
	"ID"                        INT             UNSIGNED 	NOT NULL	AUTO_INCREMENT,
	"MASTER_ID"                 VARCHAR(100)    			NOT NULL,
    "ORDER_KEY"                 VARCHAR(255)    			NOT NULL,
    "WORKFLOW_PATH"             VARCHAR(255)                NOT NULL,   
    "WORKFLOW_VERSION_ID"       VARCHAR(255)                NOT NULL,   /* #2019-06-13T08:43:29Z */
    "WORKFLOW_POSITION"         VARCHAR(255)    			NOT NULL, 	/* 1#fork_1#0 */
    "WORKFLOW_FOLDER"           VARCHAR(255)                NOT NULL,
    "WORKFLOW_NAME"             VARCHAR(255)                NOT NULL,
    "WORKFLOW_TITLE"            VARCHAR(255),                           /* TODO */
    "MAIN_PARENT_ID"            INT             UNSIGNED 	NOT NULL,	/* SOS_JS_HISTORY_ORDERS.ID of the main order */
	"PARENT_ID"                 INT             UNSIGNED	NOT NULL,   /* SOS_JS_HISTORY_ORDERS.ID of the parent order */
	"PARENT_ORDER_KEY"          VARCHAR(255)    			NOT NULL,   /* SOS_JS_HISTORY_ORDERS.ORDER_KEY */
    "HAS_CHILDREN"              TINYINT         UNSIGNED    NOT NULL,
    "RETRY_COUNTER"             INT             UNSIGNED    NOT NULL,
 	"NAME"                      VARCHAR(255)    			NOT NULL,   /* TODO */
    "TITLE"                     VARCHAR(255),               			/* TODO */
    "START_CAUSE"               VARCHAR(50)     			NOT NULL,   /* order, fork, file_trigger, setback, unskip, unstop */
    "START_TIME_PLANNED"        DATETIME,                   			/* NOT NULL ??? */
	"START_TIME"                DATETIME        			NOT NULL,
	"START_WORKFLOW_POSITION"   VARCHAR(255)    			NOT NULL,
	"START_EVENT_ID"           	CHAR(16)					NOT NULL,   /* OrderAdded eventId */
	"START_PARAMETERS"   		VARCHAR(2000),							/* TODO length */	
	"CURRENT_ORDER_STEP_ID"		INT 			UNSIGNED	NOT NULL,	/* SOS_JS_HISTORY_ORDER_STEPS.ID */
	"END_TIME"                  DATETIME,
	"END_WORKFLOW_POSITION"     VARCHAR(255),
	"END_EVENT_ID"           	CHAR(16),    							/* OrderFinisched eventId */
	"END_ORDER_STEP_ID"         INT				UNSIGNED	NOT NULL,   /* SOS_JS_HISTORY_ORDER_STEPS.ID */
    "STATUS"                    VARCHAR(10)    			    NOT NULL,   /* planned, running, finished, stopped, cancelled */
    "STATUS_TIME"               DATETIME                    NOT NULL,
    "STATE_TEXT"                VARCHAR(255),               			/* TODO */
    "ERROR"                     TINYINT         			NOT NULL,   
	"ERROR_STATUS"              VARCHAR(20),                            /* failed, disrupted ... - event outcome*/
    "ERROR_REASON"              VARCHAR(50),                            /* other ... - event outcome*/
    "ERROR_RETURN_CODE"         INT(10),        
    "ERROR_CODE"                VARCHAR(50),                			/* TODO */
	"ERROR_TEXT"                VARCHAR(255),               			/* TODO */
	"LOG_ID"                    INT             UNSIGNED    NOT NULL,   /* SOS_JS_HISTORY_LOGS.ID */
    "CONSTRAINT_HASH"			CHAR(64)					NOT NULL,   /* hash from "MASTER_ID", "START_EVENT_ID"*/
	"CREATED"                   DATETIME        			NOT NULL,
	"MODIFIED"                  DATETIME        			NOT NULL,
    /* INDEX SOS_JS_HO_INX_MIDOK("MASTER_ID","ORDER_KEY"),*/ 	/* INNODB used by history*/
	INDEX SOS_JS_HO_INX_MID("MASTER_ID"), 	/* MyISAM*/
	INDEX SOS_JS_HO_INX_OK("ORDER_KEY"), 	/* MyISAM*/
    INDEX SOS_JS_HO_INX_COSID("CURRENT_ORDER_STEP_ID"), 			/* used by history*/
    INDEX SOS_JS_HO_INX_MPID("MAIN_PARENT_ID"),
    INDEX SOS_JS_HO_INX_PID("PARENT_ID"),
    INDEX SOS_JS_HO_INX_STIME("START_TIME"),
 	CONSTRAINT SOS_JS_HO_UNIQUE UNIQUE ("CONSTRAINT_HASH"), /* used by history*/  
    PRIMARY KEY ("ID")
) ENGINE=MyISAM;

/* Table for SOS_JS_HISTORY_ORDER_STEPS */
CREATE TABLE IF NOT EXISTS SOS_JS_HISTORY_ORDER_STEPS(
	"ID"                        INT             UNSIGNED	NOT NULL	AUTO_INCREMENT,
	"MASTER_ID"                 VARCHAR(100)    			NOT NULL,
	"ORDER_KEY"                 VARCHAR(255)                NOT NULL,
    "WORKFLOW_PATH"             VARCHAR(255)                NOT NULL,           
    "WORKFLOW_VERSION_ID"       VARCHAR(255)                NOT NULL,   /* #2019-06-13T08:43:29Z */
    "WORKFLOW_POSITION"         VARCHAR(255)    			NOT NULL,	/* 1#fork_1#3 */
    "MAIN_ORDER_ID"             INT             UNSIGNED	NOT NULL,		
    "ORDER_ID"                  INT             UNSIGNED	NOT NULL,		
    "POSITION"                  INT             UNSIGNED    NOT NULL,   /* 3 - last position from WORKFLOW_POSITION */
    "RETRY_COUNTER"             INT             UNSIGNED    NOT NULL,       
    "JOB_NAME"                  VARCHAR(255)    			NOT NULL,
    "JOB_TITLE"                 VARCHAR(255),               			/* TODO */
    "AGENT_PATH"                VARCHAR(100)   		 		NOT NULL,	
	"AGENT_URI"                 VARCHAR(100)   		 		NOT NULL,	
    "START_CAUSE"               VARCHAR(50)     			NOT NULL,   /* order, file_trigger, setback, unskip, unstop */
    "START_TIME"                DATETIME        			NOT NULL,	
    "START_EVENT_ID"           	CHAR(16)					NOT NULL,   /* ProcessingStarted eventId */
	"START_PARAMETERS"   		VARCHAR(2000),							/* TODO length */	
	"END_TIME"                  DATETIME,		
    "END_EVENT_ID"           	CHAR(16),    							/* Processed eventId */
	"END_PARAMETERS"   			VARCHAR(2000),							/* TODO length */	
	"RETURN_CODE"               INT(10),		
    "STATUS"                    VARCHAR(255)    			NOT NULL,   /* running, processed */
    "ERROR"                     TINYINT         			NOT NULL,   /* TODO */
	"ERROR_STATUS"              VARCHAR(20),                            /* failed, disrupted ... - event outcome*/
    "ERROR_REASON"              VARCHAR(50),                            /* other ... - event outcome*/
    "ERROR_CODE"                VARCHAR(50),                			/* TODO */
	"ERROR_TEXT"                VARCHAR(255),               			/* TODO */
   	"LOG_ID"                    INT             UNSIGNED    NOT NULL,   /* SOS_JS_HISTORY_LOGS.ID */
 	"CONSTRAINT_HASH"			CHAR(64)					NOT NULL,   /* hash from "MASTER_ID", "START_EVENT_ID"*/
	"CREATED"                   DATETIME        			NOT NULL,
	"MODIFIED"                  DATETIME        			NOT NULL,
    /*INDEX SOS_JS_HOS_INX_MIDOK("MASTER_ID","ORDER_KEY"),*/ 		/* INNODB used by history*/
    INDEX SOS_JS_HOS_INX_MID("MASTER_ID"), 		/* MyISAM used by history*/
    INDEX SOS_JS_HOS_INX_OK("ORDER_KEY"), 		/* MyISAM used by history*/
    
    INDEX SOS_JS_HOS_INX_MOID("MAIN_ORDER_ID"),
    INDEX SOS_JS_HOS_INX_OID("ORDER_ID"),
    CONSTRAINT SOS_JS_HOS_UNIQUE UNIQUE ("CONSTRAINT_HASH"), 	/* used by history*/
    PRIMARY KEY ("ID")
) ENGINE=MyISAM;

/* Table for SOS_JS_HISTORY_ORDER_STATUS */
CREATE TABLE IF NOT EXISTS SOS_JS_HISTORY_ORDER_STATUS(
    "ID"                        INT             UNSIGNED    NOT NULL    AUTO_INCREMENT,
    "MASTER_ID"                 VARCHAR(100)                NOT NULL,
    "ORDER_KEY"                 VARCHAR(255)                NOT NULL,
    "WORKFLOW_PATH"             VARCHAR(255)                NOT NULL,           
    "WORKFLOW_VERSION_ID"       VARCHAR(255)                NOT NULL,   /* #2019-06-13T08:43:29Z */
    "WORKFLOW_POSITION"         VARCHAR(255)                NOT NULL,   /* 1#fork_1#3 */
    "MAIN_ORDER_ID"             INT             UNSIGNED    NOT NULL,       
    "ORDER_ID"                  INT             UNSIGNED    NOT NULL,       
    "ORDER_STEP_ID"             INT             UNSIGNED    NOT NULL,       
    "STATUS"                    VARCHAR(255)                NOT NULL,   /* started, cancelled, stopped, suspended, finished... */
    "STATUS_TIME"               DATETIME                    NOT NULL,
    "CONSTRAINT_HASH"           CHAR(64)                    NOT NULL,  
    "CREATED"                   DATETIME                    NOT NULL,
    /*INDEX SOS_JS_HOS_INX_MIDOK("MASTER_ID","ORDER_KEY"),*/        /* INNODB used by history*/
    INDEX SOS_JS_HOST_INX_MID("MASTER_ID"),         /* MyISAM used by history*/
    INDEX SOS_JS_HOST_INX_OK("ORDER_KEY"),          /* MyISAM used by history*/
    INDEX SOS_JS_HOST_INX_MOID("MAIN_ORDER_ID"),
    INDEX SOS_JS_HOST_INX_OID("ORDER_ID"),
    CONSTRAINT SOS_JS_HOST_UNIQUE UNIQUE ("CONSTRAINT_HASH"),   
    PRIMARY KEY ("ID")
) ENGINE=MyISAM;

/* Table for SOS_JS_LOGS */
CREATE TABLE IF NOT EXISTS SOS_JS_HISTORY_LOGS(
	"ID"                        INT             UNSIGNED	NOT NULL	AUTO_INCREMENT,
	"MASTER_ID"                 VARCHAR(100)    			NOT NULL,
	"MAIN_ORDER_ID"             INT             UNSIGNED    NOT NULL,  /* SOS_JS_HISTORY_ORDERS.MAIN_PARENT_ID */
    "ORDER_ID"                  INT             UNSIGNED    NOT NULL,  /* SOS_JS_HISTORY_ORDERS.ID */
    "ORDER_STEP_ID"             INT             UNSIGNED    NOT NULL,  /* SOS_JS_HISTORY_ORDER_STEPS.ID */
    "FILE_BASENAME"             VARCHAR(255)    			NOT NULL,
 	"FILE_SIZE_UNCOMPRESSED"    INT             UNSIGNED	NOT NULL,
    "FILE_LINES_UNCOMPRESSED"   INT             UNSIGNED	NOT NULL,
    "FILE_COMPRESSED"           LONGBLOB                    NOT NULL,
    "CREATED"                   DATETIME        			NOT NULL,
    INDEX SOS_JS_HLOG_INX_MID("MASTER_ID"),       
    INDEX SOS_JS_HLOG_INX_MOID("MAIN_ORDER_ID"),
    PRIMARY KEY ("ID")
) ENGINE=MyISAM;
