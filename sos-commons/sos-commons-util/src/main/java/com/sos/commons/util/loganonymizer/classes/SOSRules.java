package com.sos.commons.util.loganonymizer.classes;

import java.util.ArrayList;
import java.util.List;

public class SOSRules {

    private List<Rule> rules=new ArrayList<Rule>();
    private List<Rule> agent=new ArrayList<Rule>();
    private List<Rule> controller=new ArrayList<Rule>();
    private List<Rule> jocCockpit=new ArrayList<Rule>();
    
    public List<Rule> getAgent() {
        return agent;
    }
    
    public void setAgent(List<Rule> agent) {
        this.agent = agent;
    }
    
    public List<Rule> getController() {
        return controller;
    }
    
    public void setController(List<Rule> controller) {
        this.controller = controller;
    }
    
    public List<Rule> getJocCockpit() {
        return jocCockpit;
    }
    
    public void setJocCockpit(List<Rule> jocCockpit) {
        this.jocCockpit = jocCockpit;
    }

    
    public List<Rule> getRules() {
        return rules;
    }

    
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
     


}
