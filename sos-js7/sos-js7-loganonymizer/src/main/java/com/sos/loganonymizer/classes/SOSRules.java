package com.sos.loganonymizer.classes;

import java.util.ArrayList;
import java.util.List;

public class SOSRules {

    private List<Rule> rules = new ArrayList<Rule>();
 

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public SOSRules() {
        super();
     }

}
