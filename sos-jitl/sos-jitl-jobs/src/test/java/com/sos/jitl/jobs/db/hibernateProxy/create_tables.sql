set SQL_MODE=ANSI_QUOTES;

CREATE TABLE IF NOT EXISTS TEST_HIBERNATE_PROXY(
    "ID"                BIGINT(20)      UNSIGNED    NOT NULL    AUTO_INCREMENT,
    "NAME"             	VARCHAR(255)                NOT NULL,
    "VALUE"             VARCHAR(255)                NOT NULL,
    PRIMARY KEY ("ID")
) ENGINE=InnoDB;