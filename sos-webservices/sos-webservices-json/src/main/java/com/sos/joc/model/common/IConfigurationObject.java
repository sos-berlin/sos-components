package com.sos.joc.model.common;

public interface IConfigurationObject {
    
    public String getTitle();
    public void setTitle(String title);
    
    /**
     * checks if object is equal using only controller relevant properties 
     * @param other
     * @return
     */
    public boolean sufficientlyEquals(Object other);
    
}
