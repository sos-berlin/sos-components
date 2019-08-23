package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceDb;
import com.sos.joc.model.jobscheduler.DB;
import com.sos.joc.model.jobscheduler.DBState;
import com.sos.joc.model.jobscheduler.DBStateText;
import com.sos.joc.model.jobscheduler.Database;

@javax.ws.rs.Path("")
public class JobSchedulerResourceDbImpl extends JOCResourceImpl implements IJobSchedulerResourceDb {

    private static final String API_CALL = "./db";
    
    
    @Deprecated
    @Override
    public JOCDefaultResponse oldPostJobschedulerDb(String accessToken) throws Exception {
        return postJobschedulerDb(accessToken);
    }

    @Override
    public JOCDefaultResponse postJobschedulerDb(String accessToken) throws Exception {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, accessToken, "", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            Enum<Dbms> dbms = connection.getFactory().getDbms();
            String dbName = null;
            String stmt = null;
            String version = null;

            if (dbms == SOSHibernateFactory.Dbms.MSSQL) {
                dbName = "SQL Server";
                stmt = "select CONVERT(varchar(255), @@version)";
            } else if (dbms == SOSHibernateFactory.Dbms.MYSQL) {
                dbName = "MySQL";
                stmt = "select version()";
            } else if (dbms == SOSHibernateFactory.Dbms.ORACLE) {
                dbName = "Oracle";
                stmt = "select BANNER from v$version";
            } else if (dbms == SOSHibernateFactory.Dbms.PGSQL) {
                dbName = "PostgreSQL";
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

            Database database = new Database();
            database.setDbms(dbName);
            database.setVersion(version);
            database.setSurveyDate(Date.from(Instant.now()));
            DBState state = new DBState();
            // TODO DB is not always running
            state.setSeverity(0);
            state.set_text(DBStateText.RUNNING);
            database.setState(state);
            DB entity = new DB();
            entity.setDatabase(database);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
