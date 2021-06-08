package com.sos.joc.db.yade;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.model.yade.Operation;
import com.sos.joc.model.yade.Protocol;
import com.sos.joc.model.yade.ProtocolFragment;
import com.sos.joc.model.yade.TransferStateText;
import com.sos.yade.commons.Yade;

public class JocYadeFilter {

    private Set<String> controllerIds;
    private Set<Long> transferIds;
    private Set<Integer> operations;
    private Set<Integer> states;
    private Set<String> sourceHosts;
    private Set<Integer> sourceProtocols;
    private Set<String> sourceProtocolHosts;
    private Set<String> targetHosts;
    private Set<Integer> targetProtocols;
    private Set<String> targetProtocolHosts;
    private Set<String> profiles;
    private Integer limit;
    private Date dateFrom;
    private Date dateTo;
    
    private static final Map<Operation, Yade.TransferOperation> TRANSFER_OPERATION_MAP = Collections.unmodifiableMap(
            new HashMap<Operation, Yade.TransferOperation>() {

                private static final long serialVersionUID = 1L;

                {
                    put(Operation.COPY, Yade.TransferOperation.COPY);
                    put(Operation.MOVE, Yade.TransferOperation.MOVE);
                    put(Operation.GETLIST, Yade.TransferOperation.GETLIST);
                    put(Operation.RENAME, Yade.TransferOperation.RENAME);
                    put(Operation.COPYFROMINTERNET, Yade.TransferOperation.COPYFROMINTERNET);
                    put(Operation.COPYTOINTERNET, Yade.TransferOperation.COPYTOINTERNET);
                }
            });
    
    private static final Map<TransferStateText, Yade.TransferState> TRANSFER_STATE_MAP = Collections.unmodifiableMap(
            new HashMap<TransferStateText, Yade.TransferState>() {

                private static final long serialVersionUID = 1L;

                {
                    put(TransferStateText.SUCCESSFUL, Yade.TransferState.SUCCESSFUL);
                    put(TransferStateText.INCOMPLETE, Yade.TransferState.INCOMPLETE);
                    put(TransferStateText.FAILED, Yade.TransferState.FAILED);
                }
            });
    
    private static final Map<Protocol, Yade.TransferProtocol> TRANSFER_PROTOCOL_MAP = Collections.unmodifiableMap(
            new HashMap<Protocol, Yade.TransferProtocol>() {

                private static final long serialVersionUID = 1L;

                {
                    put(Protocol.LOCAL, Yade.TransferProtocol.LOCAL);
                    put(Protocol.FTP, Yade.TransferProtocol.FTP);
                    put(Protocol.FTPS, Yade.TransferProtocol.FTPS);
                    put(Protocol.SFTP, Yade.TransferProtocol.SFTP);
                    put(Protocol.HTTP, Yade.TransferProtocol.HTTP);
                    put(Protocol.HTTPS, Yade.TransferProtocol.HTTPS);
                    put(Protocol.WEBDAV, Yade.TransferProtocol.WEBDAV);
                    put(Protocol.WEBDAVS, Yade.TransferProtocol.WEBDAVS);
                    put(Protocol.SMB, Yade.TransferProtocol.SMB);
                }
            });

    public Set<String> getControllerIds() {
        return controllerIds;
    }

    public void setControllerIds(Set<String> val) {
        controllerIds = val;
    }

    public Set<Long> getTransferIds() {
        return transferIds;
    }

    public void setTransferIds(Collection<Long> transferIds) {
        if (transferIds != null) {
            this.transferIds = transferIds.stream().collect(Collectors.toSet());
        } else {
            this.transferIds = null;
        }
    }

    public Set<Integer> getOperations() {
        return operations;
    }

    public void setOperations(Collection<Operation> operations) {
        if (operations != null && !operations.isEmpty()) {
            this.operations = operations.stream().map(s -> TRANSFER_OPERATION_MAP.get(s).intValue()).collect(Collectors.toSet());
        }
    }

    public Set<Integer> getStates() {
        return states;
    }

    public void setStates(Collection<TransferStateText> states) {
        if (states != null && !states.isEmpty()) {
            this.states = states.stream().map(s -> TRANSFER_STATE_MAP.get(s).intValue()).collect(Collectors.toSet());
        }
    }
    
    public void setSources(Collection<ProtocolFragment> sources) {
        if (sources != null && !sources.isEmpty()) {
            this.sourceProtocols = sources.stream().filter(s -> s.getHost() == null).map(s -> TRANSFER_PROTOCOL_MAP.get(s.getProtocol()).intValue())
                    .collect(Collectors.toSet());
            this.sourceHosts = sources.stream().filter(s -> s.getProtocol() == null).map(ProtocolFragment::getHost).collect(Collectors.toSet());
            this.sourceProtocolHosts = sources.stream().filter(s -> s.getHost() != null && s.getProtocol() != null).map(s -> TRANSFER_PROTOCOL_MAP
                    .get(s.getProtocol()).intValue() + s.getHost()).collect(Collectors.toSet());
        }
    }
    
    public Set<Integer> getSourceProtocols() {
        return sourceProtocols;
    }
    
    public Set<String> getSourceHosts() {
        return sourceHosts;
    }
    
    public Set<String> getSourceProtocolHosts() {
        return sourceProtocolHosts;
    }
    
    public void setTargets(Collection<ProtocolFragment> targets) {
        if (targets != null && !targets.isEmpty()) {
            this.targetProtocols = targets.stream().filter(s -> s.getHost() == null).map(s -> TRANSFER_PROTOCOL_MAP.get(s.getProtocol()).intValue())
                    .collect(Collectors.toSet());
            this.targetHosts = targets.stream().filter(s -> s.getProtocol() == null).map(ProtocolFragment::getHost).collect(Collectors.toSet());
            this.targetProtocolHosts = targets.stream().filter(s -> s.getHost() != null && s.getProtocol() != null).map(s -> TRANSFER_PROTOCOL_MAP
                    .get(s.getProtocol()).intValue() + s.getHost()).collect(Collectors.toSet());
        }
    }
    
    public Set<Integer> getTargetProtocols() {
        return targetProtocols;
    }
    
    public Set<String> getTargetHosts() {
        return targetHosts;
    }
    
    public Set<String> getTargetProtocolHosts() {
        return targetProtocolHosts;
    }

    public Set<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(Collection<String> profiles) {
        if (profiles != null) {
            this.profiles = profiles.stream().collect(Collectors.toSet());
        } else {
            this.profiles = null;
        }
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
