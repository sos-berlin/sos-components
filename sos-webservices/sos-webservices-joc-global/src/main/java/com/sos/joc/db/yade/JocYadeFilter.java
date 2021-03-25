package com.sos.joc.db.yade;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class JocYadeFilter {

    String controllerId;
    List<Long> transferIds;
    Set<Integer> operations;
    Set<Integer> states;
    Set<String> sourceHosts;
    Set<Integer> sourceProtocols;
    Set<String> targetHosts;
    Set<Integer> targetProtocols;
    List<String> profiles;
    Integer limit;
    Date dateFrom;
    Date dateTo;

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public List<Long> getTransferIds() {
        return transferIds;
    }

    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }

    public Set<Integer> getOperations() {
        return operations;
    }

    public void setOperations(Set<Integer> operations) {
        this.operations = operations;
    }

    public Set<Integer> getStates() {
        return states;
    }

    public void setStates(Set<Integer> states) {
        this.states = states;
    }

    public Set<String> getSourceHosts() {
        return sourceHosts;
    }

    public void setSourceHosts(Set<String> sourceHosts) {
        this.sourceHosts = sourceHosts;
    }

    public Set<Integer> getSourceProtocols() {
        return sourceProtocols;
    }

    public void setSourceProtocols(Set<Integer> sourceProtocols) {
        this.sourceProtocols = sourceProtocols;
    }

    public Set<String> getTargetHosts() {
        return targetHosts;
    }

    public void setTargetHosts(Set<String> targetHosts) {
        this.targetHosts = targetHosts;
    }

    public Set<Integer> getTargetProtocols() {
        return targetProtocols;
    }

    public void setTargetProtocols(Set<Integer> targetProtocols) {
        this.targetProtocols = targetProtocols;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

}
