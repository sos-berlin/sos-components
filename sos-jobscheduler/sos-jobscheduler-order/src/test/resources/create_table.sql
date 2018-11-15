CREATE TABLE SOS_JS_ORDER_DAILY_PLAN(
  "ID"                    NUMBER(10)    NOT NULL,
  "MASTER_ID"             VARCHAR(100)  NOT NULL,
  "WORKFLOW"              VARCHAR(255)  DEFAULT ''.'' NOT NULL,
  "ORDER_NAME"              VARCHAR(255)  DEFAULT ''.'' NOT NULL,
  "ORDER_KEY"             VARCHAR(255)  DEFAULT ''.'' NOT NULL,
  "CALENDAR_ID"           NUMBER(10)    NOT NULL,
  "PLANNED_START"         DATE          NOT NULL,
  "EXPECTED_END"          DATE          NULL,
  "CREATED"               DATE          NOT NULL,
  "MODIFIED"              DATE          NOT NULL,    
  CONSTRAINT SOS_JS_DAILY_PLAN_UNIQUE UNIQUE ("MASTER_ID", "WORKFLOW", "ORDER_KEY"),
  PRIMARY KEY ("ID")
)
  
CREATE SOS_JS_ORDER_VARIABLES(
  "ID"                    NUMBER(10)    NOT NULL,
  "PLAN_ID"               NUMBER(10)    NOT NULL,
  "VARIABLE_NAME"         VARCHAR(100)  NOT NULL,
  "VARIABLE_VALUE"        VARCHAR(4000)  NOT NULL,
  "CREATED"               DATE          NOT NULL,
  "MODIFIED"              DATE          NOT NULL,    
  PRIMARY KEY ("ID")
)

