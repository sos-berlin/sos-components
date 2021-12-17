package com.sos.joc.db.deploy.items;

public class Deployed {

    private String name;
    private Integer type;
    
    public Deployed(String name, Integer type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getObjectType() {
        return type;
    }
}
