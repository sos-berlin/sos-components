package com.sos.joc.db.deploy.items;

public class AddOrderTags {
    
//    @JsonIgnore
//    private static final TypeReference<Map<String,Set<String>>> typeRef = new TypeReference<Map<String,Set<String>>>() {};

    private String name;
//    private Map<String, Set<String>> addOrderTags;
    private String addOrderTags;
    
    public void setName(String val) {
        name = val;
    }
    
    public String getName() {
        return name;
    }
    
    public void setAddOrderTags(String val) {
//        try {
//            addOrderTags = Globals.objectMapper.readValue(val, typeRef);
//        } catch (Exception e) {
//            addOrderTags = null;
//        }
        addOrderTags = val;
    }
    
//    public Map<String, Set<String>> getAddOrderTags() {
//        return addOrderTags;
//    }
    
    public String getAddOrderTags() {
        return addOrderTags;
    }
}
