CREATE TABLE SOS_JS_ORDER_DAILY_PLAN(
  "ID"                    NUMBER(19)    NOT NULL,
  "PLAN_ID"               NUMBER(19)    NOT NULL,
  "JOBSCHEDULER_ID"             VARCHAR(100)  NOT NULL,
  "WORKFLOW"              VARCHAR(255)  DEFAULT ''.'' NOT NULL,
  "ORDER_NAME"            VARCHAR(255)  DEFAULT ''.'' NOT NULL,
  "ORDER_KEY"             VARCHAR(255)  DEFAULT ''.'' NOT NULL,
  "CALENDAR_ID"           NUMBER(10)    NOT NULL,
  "SUBMITTED"             NUMBER(1)     DEFAULT 0 NOT NULL,
  "SUBMIT_TIME"           DATE          NULL,
  "PERIOD_BEGIN"          Date          NULL,
  "PERIOD_END"            Date          NULL,
  "REPEAT_INTERVAL"       Number(9)     NULL,
  "PLANNED_START"         DATE          NOT NULL,
  "EXPECTED_END"          DATE          NULL,
  "CREATED"               DATE          NOT NULL,
  "MODIFIED"              DATE          NOT NULL,    
  CONSTRAINT SOS_JS_DAILY_PLAN_UNIQUE UNIQUE ("JOBSCHEDULER_ID", "WORKFLOW", "ORDER_KEY"),
  PRIMARY KEY ("ID")
)

CREATE TABLE SOS_JS_ORDER_DAYS_PLANNED(
  "ID"                    NUMBER(19)    NOT NULL,
  "JOBSCHEDULER_ID"             VARCHAR(100)  NOT NULL,
  "DAY"            		  NUMBER(3)     NOT NULL,
  "YEAR"                  NUMBER(4)     NOT NULL,
  "CREATED"               DATE          NOT NULL,
  "MODIFIED"              DATE          NOT NULL,    
  CONSTRAINT SOS_JS_DAYS_PLANNED_UNIQUE UNIQUE ("JOBSCHEDULER_ID", "DAY", "YEAR"),
  PRIMARY KEY ("ID")
)
  
  
CREATE TABLE SOS_JS_ORDER_VARIABLES(
  "ID"                    NUMBER(19)    NOT NULL,
  "PLANNED_ORDER_ID"      NUMBER(10)    NOT NULL,
  "VARIABLE_NAME"         VARCHAR(100)  NOT NULL,
  "VARIABLE_VALUE"        VARCHAR(4000)  NOT NULL,
  "CREATED"               DATE          NOT NULL,
  "MODIFIED"              DATE          NOT NULL,    
  PRIMARY KEY ("ID")
)

CREATE SEQUENCE SOS_JS_DPL_SEQ
  INCREMENT BY 1 
  START WITH 1 
  MAXVALUE 999999999 
  MINVALUE 1 CYCLE

  CREATE SEQUENCE SOS_JS_DP_SEQ
  INCREMENT BY 1 
  START WITH 1 
  MAXVALUE 999999999 
  MINVALUE 1 CYCLE
  
  CREATE SEQUENCE SOS_JS_DPV_SEQ
  INCREMENT BY 1 
  START WITH 1 
  MAXVALUE 999999999 
  MINVALUE 1 CYCLE



CREATE TABLE SOS_JS_ORDER_DAYS_PLANNED(
  ID                   INT   NOT NULL AUTO_INCREMENT,
  JOBSCHEDULER_ID            VARCHAR(100)   NOT NULL,
  DAY            	   TINYINT        NOT NULL,
  YEAR                 TINYINT        NOT NULL,
  CREATED               DATETIME      NOT NULL,
  MODIFIED              DATETIME      NOT NULL,    
  CONSTRAINT SOS_JS_DAYS_PLANNED_UNIQUE UNIQUE (JOBSCHEDULER_ID, DAY, YEAR),
  PRIMARY KEY (ID)
)
 
CREATE TABLE SOS_JS_ORDER_VARIABLES(
  ID                   INT  NOT NULL AUTO_INCREMENT,
  PANNED_ORDER_ID      INT NOT NULL,
  VARIABLE_NAME         VARCHAR(100)  NOT NULL,
  VARIABLE_VALUE        VARCHAR(4000)  NOT NULL,
  CREATED               DATETIME     NOT NULL,
  MODIFIED              DATETIME      NOT NULL,    
  PRIMARY KEY (ID)
)