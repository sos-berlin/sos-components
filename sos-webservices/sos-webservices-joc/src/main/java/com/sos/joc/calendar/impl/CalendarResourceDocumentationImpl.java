package com.sos.joc.calendar.impl;

import java.sql.Date;
import java.time.Instant;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.calendar.resource.ICalendarResourceDocumentation;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AssignmentCalendarDocuAudit;
import com.sos.joc.classes.documentation.Documentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.calendar.CalendarDocuFilter;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.docu.DocumentationShowFilter;

@Path("calendar")
public class CalendarResourceDocumentationImpl extends JOCResourceImpl implements ICalendarResourceDocumentation {

    private static final String API_CALL = "./calendar/documentation";
    private static final String API_CALL_ASSIGN = API_CALL + "/assign";
    private static final String API_CALL_UNASSIGN = API_CALL + "/unassign";

    @Override
    public JOCDefaultResponse postDocumentation(String xAccessToken, String accessToken, String jobschedulerId, String path) throws Exception {
        return postDocumentation(getAccessToken(xAccessToken, accessToken), jobschedulerId, path);
    }

    public JOCDefaultResponse postDocumentation(String xAccessToken, String jobschedulerId, String path) throws Exception {
        SOSHibernateSession connection = null;
        try {
            DocumentationShowFilter documentationFilter = new DocumentationShowFilter();
            documentationFilter.setJobschedulerId(jobschedulerId);
            documentationFilter.setPath(path);
            documentationFilter.setType(JobSchedulerObjectType.WORKINGDAYSCALENDAR);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, documentationFilter, xAccessToken, documentationFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(jobschedulerId, xAccessToken).getCalendar().getView().isDocumentation());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("jobschedulerId", jobschedulerId);
            checkRequiredParameter("calendar", path);

            documentationFilter.setPath(normalizePath(documentationFilter.getPath()));
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            String docPath = dbLayer.getDocumentationPath(documentationFilter);
            if (docPath == null) {
                throw new DBMissingDataException("The documentation couldn't determine");
            }

            String entity = String.format(
                    "<!DOCTYPE html>%n<html>\n<head>%n  <meta http-equiv=\"refresh\" content=\"0;URL='../documentation/%s/%s%s'\" />%n</head>%n<body>%n</body>%n</html>",
                    documentationFilter.getJobschedulerId(), xAccessToken, JOCJsonCommand.urlEncodedPath(docPath));

            return JOCDefaultResponse.responseHtmlStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseHTMLStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseHTMLStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    @Override
    public JOCDefaultResponse assignDocu(String xAccessToken, CalendarDocuFilter filter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_ASSIGN, filter, xAccessToken, filter.getJobschedulerId(), getPermissonsJocCockpit(
                    filter.getJobschedulerId(), xAccessToken).getCalendar().isAssignDocumentation());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            AssignmentCalendarDocuAudit assignAudit = new AssignmentCalendarDocuAudit(filter);
            logAuditMessage(assignAudit);
            Documentation.assignDocu(filter.getJobschedulerId(), normalizePath(filter.getCalendar()), filter.getDocumentation(),
                    JobSchedulerObjectType.WORKINGDAYSCALENDAR, API_CALL_ASSIGN);
            storeAuditLogEntry(assignAudit);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

    @Override
    public JOCDefaultResponse unassignDocu(String xAccessToken, CalendarDocuFilter filter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_UNASSIGN, filter, xAccessToken, filter.getJobschedulerId(), getPermissonsJocCockpit(
                    filter.getJobschedulerId(), xAccessToken).getCalendar().isAssignDocumentation());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            AssignmentCalendarDocuAudit unassignAudit = new AssignmentCalendarDocuAudit(filter);
            logAuditMessage(unassignAudit);
            Documentation.unassignDocu(filter.getJobschedulerId(), normalizePath(filter.getCalendar()), JobSchedulerObjectType.WORKINGDAYSCALENDAR,
                    API_CALL_UNASSIGN);
            storeAuditLogEntry(unassignAudit);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}