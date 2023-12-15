package com.sos.joc.db.history.items;

import java.nio.charset.StandardCharsets;

public class CSVItem {
    
    private String csv;
    private String folder;
    
    public String getCsv() {
        return csv;
    }
    
    public byte[] getCsvBytes() {
        return (csv + "\n").getBytes(StandardCharsets.UTF_8);
    }
    
    public void setCsv(String val) {
        this.csv = val;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public void setFolder(String val) {
        this.folder = val;
    }

}
