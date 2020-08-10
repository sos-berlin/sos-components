package com.sos.joc.inventory.impl;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadIdResource;
import com.sos.joc.model.inventory.read.id.RequestFilter;
import com.sos.joc.model.inventory.read.id.ResponseItem;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReadIdResourceImpl extends JOCResourceImpl implements IReadIdResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            checkRequiredParameter("path", in.getPath());
            checkRequiredParameter("objectType", in.getObjectType());

            in.setPath(normalizePath(in.getPath()));

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = read(in);
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse read(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            Object id = dbLayer.getConfigurationProperty(in.getPath(), JocInventory.getType(in.getObjectType()), "id");
            session.commit();

            if (id == null) {
                throw new Exception(String.format("configuration not found: %s", SOSString.toString(in)));
            }

            ResponseItem item = new ResponseItem();
            item.setId((Long) id);
            return JOCDefaultResponse.responseStatus200(item);
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJS7Controller().getAdministration().getConfigurations().isEdit();

        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, "", permission);
        if (response == null && permission) {
            if (!folderPermissions.isPermittedForFolder(getParent(in.getPath()))) {
                return accessDeniedResponse();
            }
        }
        return response;
    }

}
