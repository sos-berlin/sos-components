package com.sos.joc.jocs.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.controller.States;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jocs.resource.IJocsResource;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.controller.ComponentStateText;
import com.sos.joc.model.controller.ConnectionStateText;
import com.sos.joc.model.controller.OperatingSystem;
import com.sos.joc.model.joc.Cockpit;
import com.sos.joc.model.joc.CockpitFilter;
import com.sos.joc.model.joc.Cockpits;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("jocs")
public class JocsImpl extends JOCResourceImpl implements IJocsResource {

    private static final String API_CALL = "./jocs";

    @Override
    public JOCDefaultResponse postJocs(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, CockpitFilter.class);
            CockpitFilter in = Globals.objectMapper.readValue(filterBytes, CockpitFilter.class);

            // TODO what permission should be used
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Cockpits entity = new Cockpits();

            entity.setJocs(setCockpits(in));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private List<Cockpit> setCockpits(CockpitFilter in) throws DBConnectionRefusedException, DBInvalidDataException {
        SOSHibernateSession connection = null;
        
        try {
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocInstancesDBLayer dbLayer = new JocInstancesDBLayer(connection);
            
            List<DBItemJocInstance> instances = null;
            if (in.getOnlyApiServer() && !in.getOnlyNonApiServer()) {
                instances = dbLayer.getApiInstances();
            } else if (!in.getOnlyApiServer() && in.getOnlyNonApiServer()) {
                instances = dbLayer.getJocInstances();
            } else {
                instances = dbLayer.getInstances();
            }
            List<Cockpit> cockpits = new ArrayList<>();
            String curMemberId = Globals.getMemberId();
            if (instances != null) {
                InventoryOperatingSystemsDBLayer dbOsLayer = new InventoryOperatingSystemsDBLayer(connection);
                List<DBItemInventoryOperatingSystem> operatingSystems = dbOsLayer.getOSItems(instances.stream().map(DBItemJocInstance::getOsId).filter(
                        Objects::nonNull).collect(Collectors.toSet()));
                Map<Long, DBItemInventoryOperatingSystem> osMap = null;
                if (operatingSystems != null) {
                    osMap = operatingSystems.stream().collect(Collectors.toMap(DBItemInventoryOperatingSystem::getId, Function.identity()));
                }
                long nowSeconds = Instant.now().getEpochSecond();
                // TODO version should be in database
                
                for (DBItemJocInstance instance : instances) {
                    Cockpit cockpit = new Cockpit();
                    cockpit.setId(instance.getId());
                    cockpit.setInstanceId(instance.getClusterId() + "#" + instance.getOrdering());
                    cockpit.setIsApiServer(instance.getApiServer());
                    cockpit.setMemberId(instance.getMemberId());
                    cockpit.setCurrent(curMemberId.equals(instance.getMemberId()));
                    cockpit.setUrl(instance.getUri());
                    cockpit.setControllerConnectionStates(null);
                    if (osMap != null) {
                        DBItemInventoryOperatingSystem osDB = osMap.get(instance.getOsId());
                        if (osDB != null) {
                            cockpit.setHost(osDB.getHostname());
                            OperatingSystem os = new OperatingSystem();
                            os.setArchitecture(osDB.getArchitecture());
                            os.setDistribution(osDB.getDistribution());
                            os.setName(osDB.getName());
                            cockpit.setOs(os);
                        }
                    }
                    try {
                        cockpit.setSecurityLevel(JocSecurityLevel.fromValue(instance.getSecurityLevel().toUpperCase()));
                    } catch (Exception e) {
                        cockpit.setSecurityLevel(JocSecurityLevel.LOW);
                    }
                    cockpit.setStartedAt(instance.getStartedAt());
                    cockpit.setTitle(instance.getTitle());
                    // TODO only temp. delete Globals.curVersion later 
                    cockpit.setVersion(instance.getVersion() == null ? Globals.curVersion : instance.getVersion());
                    cockpit.setLastHeartbeat(instance.getHeartBeat());

                    // determine ComponentState/ConnectionState depends on last heart beat
                    cockpit.setComponentState(States.getComponentState(ComponentStateText.operational));
                    cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.established));

                    if (cockpit.getLastHeartbeat() == null) {
                        if (!cockpit.getCurrent()) {
                            cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unknown));
                            cockpit.setComponentState(States.getComponentState(ComponentStateText.unknown));
                        }
                    } else {
                        long heartBeatSeconds = cockpit.getLastHeartbeat().toInstant().getEpochSecond();
                        if (nowSeconds - heartBeatSeconds <= 31) {
                            // retain unchanged established
                        } else if (nowSeconds - heartBeatSeconds <= 61) {
                            cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unstable));
                        } else {
                            if (!cockpit.getCurrent()) {
                                cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unknown));
                                cockpit.setComponentState(States.getComponentState(ComponentStateText.unknown));
                            } else {
                                cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unstable));
                            }
                        }
                    }

                    cockpits.add(cockpit);
                }
            }
            return cockpits;
        } finally {
            Globals.disconnect(connection);
        }
    }

}
