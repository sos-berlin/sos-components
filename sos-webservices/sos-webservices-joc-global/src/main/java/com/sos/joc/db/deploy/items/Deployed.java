package com.sos.joc.db.deploy.items;

public class Deployed {

    private String name;
    private Integer type;
    private Long invCId;
    
    public Deployed(Long invCId, String name, Integer type) {
        this.name = name;
        this.type = type;
        this.invCId = invCId;
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getObjectType() {
        return type;
    }
    
    public Long getInvCId() {
        return invCId;
    }
}
