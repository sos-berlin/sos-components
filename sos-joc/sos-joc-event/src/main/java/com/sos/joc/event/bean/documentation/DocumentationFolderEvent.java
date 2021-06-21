package com.sos.joc.event.bean.documentation;

public class DocumentationFolderEvent extends DocumentationEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public DocumentationFolderEvent() {
    }

    /**
     * @param folder
     */
    public DocumentationFolderEvent(String folder) {
        super("DocumentationFolderUpdated", folder);
    }
}
