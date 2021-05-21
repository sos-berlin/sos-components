package com.sos.joc.db.documentation;

import com.sos.joc.model.common.InventoryObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DocumentationOfObject {
    private String docPath;
    private String objPath;
    private InventoryObject docUsage;
    
    public DocumentationOfObject(String docPath, String objPath) {
        this.docPath = docPath;
        this.objPath = objPath;
    }
    
    public DocumentationOfObject(String docPath, String objPath, String objType) {
        this.docPath = docPath;
        this.docUsage = new InventoryObject();
        try {
            this.docUsage.setType(ConfigurationType.fromValue(objType));
        } catch (IllegalArgumentException e) {
            this.docUsage.setType(null);
        }
        this.docUsage.setPath(objPath);
    }

    public String getDocPath() {
        return docPath;
    }
    
    public String getObjPath() {
        return objPath;
    }
    
    public InventoryObject getDocUsage() {
        return docUsage;
    }

}
