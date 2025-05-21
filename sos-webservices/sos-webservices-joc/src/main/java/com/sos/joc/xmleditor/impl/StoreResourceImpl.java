package com.sos.joc.xmleditor.impl;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXmlHashComparator;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.store.StoreConfiguration;
import com.sos.joc.model.xmleditor.store.StoreConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.Utils;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.IStoreResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class StoreResourceImpl extends ACommonResourceImpl implements IStoreResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, StoreConfiguration.class);
            StoreConfiguration in = Globals.objectMapper.readValue(filterBytes, StoreConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
            if (response != null) {
                return response;
            }
            JocXmlEditor.parseXml(in.getConfiguration());

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.beginTransaction();
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            DBItemXmlEditorConfiguration item = null;
            String name = null;
            switch (in.getObjectType()) {
            case YADE:
            case OTHER:
                name = in.getName();
                if (in.getId() != null && in.getId() > 0) {
                    item = dbLayer.getObject(in.getId());
                }
                break;
            default:
                name = StandardSchemaHandler.getDefaultConfigurationName(in.getObjectType());
                item = dbLayer.getObject(in.getObjectType().name(), name);
                break;
            }

            if (item == null) {
                item = create(session, in, name, getAccount(), 0L); // TODO auditlogId

            } else {
                item = update(session, in, item, name, getAccount(), 0L); // TODO auditlogId
            }

            session.commit();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getSuccess(in.getObjectType(), item)));

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

    public static DBItemXmlEditorConfiguration create(SOSHibernateSession session, StoreConfiguration in, String name, String account,
            Long auditLogId) throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setType(in.getObjectType().name());
        item.setName(name.trim());
        item.setConfigurationDraft(in.getConfiguration());
        item.setConfigurationDraftJson(Utils.serialize(in.getConfigurationJson()));
        item.setSchemaLocation(JocXmlEditor.getSchemaLocation4Db(in.getObjectType(), in.getSchemaIdentifier()));
        item.setAuditLogId(auditLogId);
        item.setAccount(account);
        item.setCreated(new Date());
        item.setModified(item.getCreated());
        session.save(item);
        return item;
    }

    public static DBItemXmlEditorConfiguration update(SOSHibernateSession session, StoreConfiguration in, DBItemXmlEditorConfiguration item,
            String name, String account, Long auditLogId) throws Exception {
        String currentConfiguration = SOSString.isEmpty(in.getConfiguration()) ? null : in.getConfiguration();
        if (currentConfiguration != null) {
            if (item.getConfigurationDraft() != null) {
                if (SOSXmlHashComparator.equals(currentConfiguration, item.getConfigurationDraft())) {
                    return item;
                }
            } else if (item.getConfigurationReleased() != null) {
                if (SOSXmlHashComparator.equals(currentConfiguration, item.getConfigurationReleased())) {
                    return item;
                }
            }
        }

        item.setName(name.trim());
        item.setConfigurationDraft(currentConfiguration);
        item.setConfigurationDraftJson(Utils.serialize(in.getConfigurationJson()));
        item.setSchemaLocation(JocXmlEditor.getSchemaLocation4Db(in.getObjectType(), in.getSchemaIdentifier()));
        item.setAuditLogId(auditLogId);
        item.setAccount(account);
        item.setModified(new Date());
        session.update(item);
        return item;
    }

    private void checkRequiredParameters(final StoreConfiguration in) throws Exception {
        switch (in.getObjectType()) {
        case NOTIFICATION:
            break;
        case YADE:
            checkRequiredParameter("id", in.getId());
            checkRequiredParameter("name", in.getName());
            break;
        case OTHER:
            checkRequiredParameter("id", in.getId());
            checkRequiredParameter("name", in.getName());
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
            break;
        }
    }

    public static StoreConfigurationAnswer getSuccess(ObjectType type, DBItemXmlEditorConfiguration item) {
        StoreConfigurationAnswer answer = new StoreConfigurationAnswer();
        answer.setId(item.getId());
        answer.setName(item.getName());
        answer.setModified(item.getModified());
        if (JocXmlEditor.isStandardType(type)) {
            answer.setReleased(false);
            if (item.getReleased() == null) {
                answer.setHasReleases(true);
                answer.setState(ItemStateEnum.RELEASE_NOT_EXIST);
            } else {
                answer.setHasReleases(false);
                answer.setState(ItemStateEnum.DRAFT_IS_NEWER);
            }
        }
        return answer;
    }

}
