
package com.sos.inventory.model.instruction;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstructionType {

    EXECUTE_NAMED("Execute.Named"),
    IF("If"),
    TRY("Try"),
    FORK("Fork"),
    FORKLIST("ForkList"),
    FINISH("Finish"),
    FAIL("Fail"),
    RETRY("Retry"),
    LOCK("Lock"),
    PROMPT("Prompt"),
    POST_NOTICE("PostNotice"),
    POST_NOTICES("PostNotices"),
    EXPECT_NOTICE("ExpectNotice"),
    EXPECT_NOTICES("ExpectNotices"),
    CONSUME_NOTICES("ConsumeNotices"),
    IMPLICIT_END("ImplicitEnd"),
    ADD_ORDER("AddOrder"),
    CYCLE("Cycle"),
    STICKY_SUBAGENT("StickySubagent"),
    OPTIONS("Options"),
    BREAK("Break"),
    CASE_WHEN("CaseWhen"),
    SLEEP("Sleep");
    private final String value;
    private final static Map<String, InstructionType> CONSTANTS = new HashMap<String, InstructionType>();

    static {
        for (InstructionType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private InstructionType(String value) {
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
    public static InstructionType fromValue(String value) {
        InstructionType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
