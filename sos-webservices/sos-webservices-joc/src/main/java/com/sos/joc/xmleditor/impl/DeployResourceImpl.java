package com.sos.joc.xmleditor.impl;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.XmlEditorAudit;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.classes.xmleditor.exceptions.XsdValidatorException;
import com.sos.joc.classes.xmleditor.validator.XsdValidator;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.AnswerMessage;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.deploy.DeployConfiguration;
import com.sos.joc.model.xmleditor.deploy.DeployConfigurationAnswer;
import com.sos.joc.xmleditor.resource.IDeployResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class DeployResourceImpl extends JOCResourceImpl implements IDeployResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployResourceImpl.class);

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            JsonValidator.validateFailFast(filterBytes, DeployConfiguration.class);
            DeployConfiguration in = Globals.objectMapper.readValue(filterBytes, DeployConfiguration.class);

            if (in.getObjectType() != null && in.getObjectType().equals(ObjectType.OTHER)) {
                throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                        "[%s][%s]unsupported object type for deployment", in.getJobschedulerId(), in.getObjectType().name())));
            }
            // TODO check folder permissions
            checkRequiredParameters(in);

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                XmlEditorAudit audit = new XmlEditorAudit(in);
                logAuditMessage(audit);

                // step 1 - check for vulnerabilities and validate
                XsdValidator validator = new XsdValidator(JocXmlEditor.getStandardAbsoluteSchemaLocation(in.getObjectType()));
                try {
                    validator.validate(in.getConfiguration());
                } catch (XsdValidatorException e) {
                    LOGGER.error(String.format("[%s]%s", validator.getSchema(), e.toString()), e);
                    return JOCDefaultResponse.responseStatus200(ValidateResourceImpl.getError(e));
                }

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

                // step 2 - store draft in the database
                DBItemXmlEditorConfiguration item = getItem(dbLayer, in);

                // step 3 - deploy
                Map<Date, String> result = putFile(in, item.getConfigurationDraft());

                // step 4 - update db
                Date deployed = result.keySet().iterator().next();
                audit.setStartTime(deployed);
                // DBItemAuditLog auditItem = storeAuditLogEntry(audit);
                // if (auditItem != null) {
                // item.setAuditLogId(auditItem.getId());
                // }
                item.setConfigurationDeployed(result.get(deployed));
                item.setConfigurationDeployedJson(item.getConfigurationDraftJson());
                item.setConfigurationDraft(null);
                item.setConfigurationDraftJson(null);
                item.setAccount(getAccount());
                item.setDeployed(deployed);
                item.setModified(new Date());

                session.beginTransaction();
                session.update(item);
                session.commit();
                response = JOCDefaultResponse.responseStatus200(getSuccess(deployed));
            }
            return response;
        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private void checkRequiredParameters(final DeployConfiguration in) throws Exception {
        checkRequiredParameter("jobschedulerId", in.getJobschedulerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        checkRequiredParameter("configuration", in.getConfiguration());
        checkRequiredParameter("configurationJson", in.getConfigurationJson());
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final DeployConfiguration in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit(in.getJobschedulerId(), accessToken);
        boolean permission = permissions.getJobschedulerMaster().getAdministration().isEditPermissions();
        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, in.getJobschedulerId(), permission);
        if (response == null) {
            if (versionIsOlderThan(JocXmlEditor.AVAILABILITY_STARTING_WITH)) {
                throw new JobSchedulerBadRequestException(JocXmlEditor.MESSAGE_UNSUPPORTED_WEB_SERVICE);
            }
        }
        return response;
    }

    private DBItemXmlEditorConfiguration getItem(DbLayerXmlEditor dbLayer, DeployConfiguration in) throws Exception {
        dbLayer.getSession().beginTransaction();
        DBItemXmlEditorConfiguration item = dbLayer.getObject(in.getJobschedulerId(), in.getObjectType().name(), JocXmlEditor.getConfigurationName(in
                .getObjectType()));
        if (item == null) {
            item = new DBItemXmlEditorConfiguration();
            item.setSchedulerId(in.getJobschedulerId());
            item.setObjectType(in.getObjectType().name());
            item.setName(JocXmlEditor.getConfigurationName(in.getObjectType()));
            item.setConfigurationDraft(in.getConfiguration());
            item.setConfigurationDraftJson(in.getConfigurationJson());
            item.setSchemaLocation(JocXmlEditor.getStandardRelativeSchemaLocation(in.getObjectType()));

            item.setAuditLogId(new Long(0));// TODO
            item.setAccount(getAccount());
            item.setCreated(new Date());
            item.setModified(item.getCreated());
            dbLayer.getSession().save(item);
        } else {
            item.setConfigurationDraft(in.getConfiguration());
            item.setConfigurationDraftJson(in.getConfigurationJson());
            item.setAccount(getAccount());
            item.setModified(new Date());
            dbLayer.getSession().update(item);
        }
        dbLayer.getSession().commit();
        return item;
    }

    private DeployConfigurationAnswer getSuccess(Date deployed) {
        DeployConfigurationAnswer answer = new DeployConfigurationAnswer();
        answer.setDeployed(deployed);
        answer.setMessage(new AnswerMessage());
        answer.getMessage().setCode(JocXmlEditor.MESSAGE_CODE_DRAFT_NOT_EXIST);
        answer.getMessage().setMessage(JocXmlEditor.MESSAGE_DRAFT_NOT_EXIST);
        return answer;
    }

    private Map<Date, String> putFile(DeployConfiguration in, String configuration) throws Exception {
        throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_DEPLOY_ERROR, "not implemented yet"));
    }

}
