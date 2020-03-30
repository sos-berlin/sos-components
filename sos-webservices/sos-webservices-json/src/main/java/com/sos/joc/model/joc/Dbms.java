
package com.sos.joc.model.joc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Dbms {

    SQL_SERVER("SQL Server"),
    MY_SQL("MySQL"),
    ORACLE("Oracle"),
    POSTGRE_SQL("PostgreSQL");
    private final String value;
    private final static Map<String, Dbms> CONSTANTS = new HashMap<String, Dbms>();

    static {
        for (Dbms c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Dbms(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Dbms fromValue(String value) {
        Dbms constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
