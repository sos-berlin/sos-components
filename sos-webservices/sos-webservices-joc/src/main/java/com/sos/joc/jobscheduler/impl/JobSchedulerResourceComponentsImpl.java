package com.sos.joc.jobscheduler.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.MasterAnswer;
import com.sos.joc.classes.jobscheduler.States;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceComponents;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.jobscheduler.ClusterNodeStateText;
import com.sos.joc.model.jobscheduler.ComponentStateText;
import com.sos.joc.model.jobscheduler.Components;
import com.sos.joc.model.jobscheduler.ConnectionStateText;
import com.sos.joc.model.jobscheduler.Master;
import com.sos.joc.model.jobscheduler.Role;
import com.sos.joc.model.joc.Cockpit;
import com.sos.joc.model.joc.DB;
import com.sos.schema.JsonValidator;

@Path("jobscheduler")
public class JobSchedulerResourceComponentsImpl extends JOCResourceImpl implements IJobSchedulerResourceComponents {

    private static final String API_CALL = "./jobscheduler/components";

    @Override
    public JOCDefaultResponse postComponents(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;

        try {
            JsonValidator.validateFailFast(filterBytes, JobSchedulerId.class);
            JobSchedulerId jobSchedulerFilter = Globals.objectMapper.readValue(filterBytes, JobSchedulerId.class);

            checkRequiredParameter("jobschedulerId", jobSchedulerFilter.getJobschedulerId());

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobSchedulerFilter, accessToken, jobSchedulerFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(jobSchedulerFilter.getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);

            Components entity = new Components();

            entity.setDatabase(getDB(connection));
            Cockpit cockpit = new Cockpit();
            cockpit.setVersion(readVersion());
            // TODO different componentStates
            cockpit.setComponentState(States.getComponentState(ComponentStateText.operational));
            entity.setJoc(cockpit);
            List<MasterAnswer> masters = JobSchedulerResourceMastersImpl.getMasterAnswers(jobSchedulerFilter.getJobschedulerId(), accessToken,
                    connection);
            ClusterType clusterType = getClusterType(masters);
            entity.setClusterState(States.getClusterState(clusterType));
            entity.setMasters(masters.stream().map(Master.class::cast).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    private static String readVersion() {
        InputStream stream = null;
        String versionFile = "/version.json";
        try {
            stream = JobSchedulerResourceComponentsImpl.class.getClassLoader().getResourceAsStream(versionFile);
            if (stream != null) {
                return Json.createReader(stream).readObject().getString("version", "unknown");
            }
        } catch (Exception e) {
            //
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
        return "unknown";
    }

    private static DB getDB(SOSHibernateSession connection) throws SOSHibernateException {
        Enum<Dbms> dbms = connection.getFactory().getDbms();
        String stmt = null;
        String version = null;
        DB db = new DB();

        if (dbms == SOSHibernateFactory.Dbms.MSSQL) {
            db.setDbms("SQL Server");
            stmt = "select CONVERT(varchar(255), @@version)";
        } else if (dbms == SOSHibernateFactory.Dbms.MYSQL) {
            db.setDbms("MySQL");
            stmt = "select version()";
        } else if (dbms == SOSHibernateFactory.Dbms.ORACLE) {
            db.setDbms("Oracle");
            stmt = "select BANNER from v$version";
        } else if (dbms == SOSHibernateFactory.Dbms.PGSQL) {
            db.setDbms("PostgreSQL");
            stmt = "show server_version";
        }

        if (stmt != null) {
            List<String> result = connection.getResultListNativeQuery(stmt);
            if (!result.isEmpty()) {
                version = result.get(0);
                if (version.contains("\n")) {
                    version = version.substring(0, version.indexOf("\n"));
                }
            }
            if (version != null) {
                if (dbms == SOSHibernateFactory.Dbms.MSSQL) {
                    // only first line
                    version = version.trim().split("\r?\n", 2)[0];
                }
                version = version.trim();
            }
        }
        // TODO different states
        db.setComponentState(States.getComponentState(ComponentStateText.operational));
        db.setConnectionState(States.getConnectionState(ConnectionStateText.established));
        db.setVersion(version);

        return db;
    }

    private static ClusterType getClusterType(List<MasterAnswer> masters) {
        ClusterType clusterType = null;
        if (!masters.stream().filter(m -> m.getRole() == Role.STANDALONE).findAny().isPresent()) {
            int unreachables = masters.stream().filter(m -> m.getConnectionState().get_text() == ConnectionStateText.unreachable).mapToInt(m -> 1)
                    .sum();
            if (unreachables == masters.size()) {
                //
            } else if (unreachables == 0) {
                Optional<MasterAnswer> j = masters.stream().filter(m -> m.getClusterNodeState().get_text() == ClusterNodeStateText.active).findAny();
                if (j.isPresent()) {
                    clusterType = j.get().getClusterState();
                } else {
                    clusterType = masters.get(0).getClusterState();
                }
            } else {
                MasterAnswer j = masters.stream().filter(m -> m.getConnectionState().get_text() != ConnectionStateText.unreachable).findAny().get();
                clusterType = j.getClusterState();
                if (j.isCoupledOrPreparedTobeCoupled()) {
                    int index = masters.indexOf(j);
                    int otherIndex = (index + 1) % 2;
                    if (j.getClusterNodeState().get_text() == ClusterNodeStateText.active) {
                        masters.get(otherIndex).setClusterNodeState(States.getClusterNodeState(false, true));
                    } else {
                        masters.get(otherIndex).setClusterNodeState(States.getClusterNodeState(true, true));
                    }
                    if (j.getClusterState() == ClusterType.PREPARED_TO_BE_COUPLED) {
                        masters.get(otherIndex).setComponentState(States.getComponentState(ComponentStateText.inoperable));
                    }
                    if (j.getClusterState() == ClusterType.COUPLED) {
                        masters.get(otherIndex).setIsCoupled(true);
                    }
                }
            }
        }
        return clusterType;
    }

}
