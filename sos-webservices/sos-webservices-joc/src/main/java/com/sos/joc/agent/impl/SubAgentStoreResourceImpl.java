package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.agent.resource.ISubAgentStoreResource;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingLicenseException;
import com.sos.joc.model.agent.StoreSubAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentRef;
import reactor.core.publisher.Flux;

@Path("agent")
public class SubAgentStoreResourceImpl extends JOCResourceImpl implements ISubAgentStoreResource {

    private static String API_CALL_REMOVE = "./agent/subagents/store";

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REMOVE, filterBytes, accessToken);

            if (JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
                throw new JocMissingLicenseException("missing license for Agent cluster");
            }

            JsonValidator.validateFailFast(filterBytes, StoreSubAgents.class);
            StoreSubAgents subAgentsParam = Globals.objectMapper.readValue(filterBytes, StoreSubAgents.class);

            String controllerId = subAgentsParam.getControllerId();
            String agentId = subAgentsParam.getAgentId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(subAgentsParam.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            
            DBItemInventoryAgentInstance dbAgent = dbLayer.getAgentInstance(agentId);
            if (dbAgent == null) {
                throw new JocBadRequestException("Cluster Agent '" + agentId + "' doesn't exist");
            }

            Map<String, SubAgent> subAgentsMap = subAgentsParam.getSubagents().stream().distinct().collect(Collectors.toMap(SubAgent::getSubagentId,
                    Function.identity(), (k, v) -> v));
            List<DBItemInventorySubAgentInstance> dbSubAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(controllerId));
            
            List<JUpdateItemOperation> subAgentsToController = saveOrUpdate(dbLayer, dbAgent, dbSubAgents, subAgentsMap);
            Globals.commit(connection);
            Globals.disconnect(connection);
            connection = null;
            
            if (!subAgentsToController.isEmpty()) {
                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(subAgentsToController)).thenAccept(e -> ProblemHelper
                        .postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(connection);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(connection);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    public static List<JUpdateItemOperation> saveOrUpdate(InventoryAgentInstancesDBLayer dbLayer, DBItemInventoryAgentInstance dbAgent,
            Collection<DBItemInventorySubAgentInstance> dbSubAgents, Map<String, SubAgent> subAgentsMap) throws SOSHibernateException {
        String agentId = dbAgent.getAgentId();
        
        Map<Boolean, List<DBItemInventorySubAgentInstance>> mapOfAgentIds = dbSubAgents.stream().collect(Collectors.groupingBy(s -> s.getAgentId()
                .equals(agentId)));
        
        // checks if subagentId from request is used in other agentIds
        mapOfAgentIds.getOrDefault(false, Collections.emptyList()).parallelStream().filter(s -> subAgentsMap.keySet().contains(s.getSubAgentId()))
                .findAny().ifPresent(s -> {
                    throw new JocBadRequestException("subagentId has to be unique per controller: '" + s.getSubAgentId()
                            + "' is already used in Agent '" + s.getAgentId() + "'");
                });
        
        // checks java name rules of SubagentIds
        subAgentsMap.keySet().forEach(id -> {
            CheckJavaVariableName.test("Subagent ID", id);
        });
        
        // checks that director and standby director can only exist once
        List<SubagentDirectorType> direcs = Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR, SubagentDirectorType.STANDBY_DIRECTOR);
        Predicate<SubAgent> isDirector = s -> direcs.contains(s.getIsDirector());
        subAgentsMap.values().stream().filter(isDirector).collect(Collectors.groupingBy(SubAgent::getIsDirector, Collectors.counting())).forEach((k,
                v) -> {
            String directorType = k.equals(SubagentDirectorType.PRIMARY_DIRECTOR) ? "primary" : "standby";
            if (v > 1L) {
                throw new JocBadRequestException("At most one SubAgent can be a " + directorType + " director");
            }
        });

        // TODO check URL uniqueness?
        
        DBItemInventorySubAgentInstance primaryDirector = dbLayer.getDirectorInstance(agentId, SubagentDirectorType.PRIMARY_DIRECTOR.intValue());
        DBItemInventorySubAgentInstance standbyDirector = dbLayer.getDirectorInstance(agentId, SubagentDirectorType.STANDBY_DIRECTOR.intValue());
        
        boolean primaryDirectorIsChanged = false;
        boolean standbyDirectorIsChanged = false;
        String primaryDirectorUrl = null;
        Date now = Date.from(Instant.now());
        List<JUpdateItemOperation> subAgentsToController = new ArrayList<>();

        for (DBItemInventorySubAgentInstance dbSubAgent : mapOfAgentIds.getOrDefault(true, Collections.emptyList())) {
            SubAgent subAgent = subAgentsMap.remove(dbSubAgent.getSubAgentId());
            if (subAgent == null) {
                continue;
            }
            if (dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector()) && dbSubAgent.getUri().equals(subAgent.getUrl())) {
                continue;
            }
            if (subAgent.getIsDirector().equals(SubagentDirectorType.PRIMARY_DIRECTOR)) {
                if (primaryDirectorIsChanged) {
                    throw new JocBadRequestException("At most one SubAgent can be a director");
                } else if (!dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector())) {
                    primaryDirectorIsChanged = true;
                    primaryDirectorUrl = subAgent.getUrl();
                }
            }
            if (subAgent.getIsDirector().equals(SubagentDirectorType.STANDBY_DIRECTOR)) {
                if (standbyDirectorIsChanged) {
                    throw new JocBadRequestException("At most one SubAgent can be a standby director");
                } else if (!dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector())) {
                    standbyDirectorIsChanged = true;
                }
            }
            if (!dbSubAgent.getUri().equals(subAgent.getUrl())) {
                subAgentsToController.add(JUpdateItemOperation.addOrChangeSimple(JSubagentRef.of(SubagentId.of(subAgent.getSubagentId()), AgentPath
                        .of(agentId), Uri.of(subAgent.getUrl()))));
            }
            dbSubAgent.setIsDirector(subAgent.getIsDirector());
            dbSubAgent.setUri(subAgent.getUrl());
            dbSubAgent.setOrdering(subAgent.getOrdering());
            dbSubAgent.setModified(now);
            dbLayer.getSession().update(dbSubAgent);

        }
        for (SubAgent subAgent : subAgentsMap.values()) {
            if (subAgent.getIsDirector().equals(SubagentDirectorType.PRIMARY_DIRECTOR)) {
                primaryDirectorIsChanged = true;
                primaryDirectorUrl = subAgent.getUrl();
            }
            if (subAgent.getIsDirector().equals(SubagentDirectorType.STANDBY_DIRECTOR)) {
                standbyDirectorIsChanged = true;
            }
            DBItemInventorySubAgentInstance dbSubAgent = new DBItemInventorySubAgentInstance();
            dbSubAgent.setId(null);
            dbSubAgent.setAgentId(agentId);
            dbSubAgent.setSubAgentId(subAgent.getSubagentId());
            dbSubAgent.setIsDirector(subAgent.getIsDirector());
            dbSubAgent.setOrdering(subAgent.getOrdering());
            dbSubAgent.setUri(subAgent.getUrl());
            dbSubAgent.setIsWatcher(false);
            dbSubAgent.setOsId(0L);
            dbSubAgent.setModified(now);
            dbLayer.getSession().save(dbSubAgent);

            subAgentsToController.add(JUpdateItemOperation.addOrChangeSimple(JSubagentRef.of(SubagentId.of(subAgent.getSubagentId()), AgentPath.of(
                    agentId), Uri.of(subAgent.getUrl()))));
        }

        if (primaryDirectorIsChanged || standbyDirectorIsChanged) {
            if (primaryDirectorIsChanged) {
                if (primaryDirector != null) {
                    primaryDirector.setIsDirector(SubagentDirectorType.NO_DIRECTOR.intValue());
                    primaryDirector.setModified(now);
                    dbLayer.getSession().update(primaryDirector);
                }
                if (primaryDirectorUrl != null) {
                    dbAgent.setUri(primaryDirectorUrl);
                    dbLayer.updateAgent(dbAgent);
                }
            }
            if (standbyDirectorIsChanged && standbyDirector != null) {
                standbyDirector.setIsDirector(SubagentDirectorType.NO_DIRECTOR.intValue());
                standbyDirector.setModified(now);
                dbLayer.getSession().update(standbyDirector); 
            }
            List<SubagentId> directors = dbLayer.getDirectorSubAgentIds(agentId).stream().map(id -> SubagentId.of(id)).collect(Collectors.toList());
            JUpdateItemOperation agent = JUpdateItemOperation.addOrChangeSimple(JAgentRef.of(AgentPath.of(agentId), directors));
            subAgentsToController.add(agent);
        }
        return subAgentsToController;
    }
};