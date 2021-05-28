package com.sos.joc.event.bean.documentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class DocumentationEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public DocumentationEvent() {
    }

    /**
     * @param folder
     */
    public DocumentationEvent(String folder) {
        super("DocumentationUpdated", null, null);
        putVariable("folder", folder);
    }
    
    public DocumentationEvent(String key, String folder) {
        super(key, null, null);
        putVariable("folder", folder);
    }
    
    @JsonIgnore
    public String getFolder() {
        return getVariables().get("folder");
    }
}
