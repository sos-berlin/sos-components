package com.sos.joc.event.bean.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(ReportsUpdated.class), 
    @JsonSubTypes.Type(ReportRunsUpdated.class)
})

public abstract class ReportingEvent extends JOCEvent {

    public ReportingEvent(String key) {
        super(key, null, null);
    }

}
