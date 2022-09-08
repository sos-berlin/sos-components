package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IValidateNameResource;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ValidateNameResourceImpl extends JOCResourceImpl implements IValidateNameResource {

    @Override
    public JOCDefaultResponse check(final String accessToken, byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = validate(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse validate(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            
            checkRequiredParameter("path", in.getPath());
            checkRequiredParameter("objectType", in.getObjectType());
            java.nio.file.Path path = JocInventory.normalizePath(in.getPath());
            String p = path.toString().replace('\\', '/');
            
            // Check Java variable name rules
            for (int i = 0; i < path.getNameCount(); i++) {
                if (i == path.getNameCount() - 1) {
                    SOSCheckJavaVariableName.test("name", path.getName(i).toString());
                } else {
                    SOSCheckJavaVariableName.test("folder", path.getName(i).toString());
                }
            }
            
            // Check folder permissions
            if (JocInventory.isFolder(in.getObjectType()) && !folderPermissions.isPermittedForFolder(p)) {
                throw new JocFolderPermissionsException("Access denied for folder: " + p);
            }
            
            // check if name is unique
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            if (JocInventory.isFolder(in.getObjectType())) {
                DBItemInventoryConfiguration item = dbLayer.getConfiguration(p, in.getObjectType().intValue());
                if (item != null) {
                    throw new JocObjectAlreadyExistException(String.format("The path '%s' is already used", p));
                }
            } else {
                String name = path.getFileName().toString();
                List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(name, in.getObjectType().intValue());
                if (namedItems != null && !namedItems.isEmpty()) {
                    throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", name,
                            namedItems.get(0).getPath()));
                }
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
