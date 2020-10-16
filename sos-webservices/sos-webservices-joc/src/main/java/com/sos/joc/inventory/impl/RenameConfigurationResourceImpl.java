package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IRenameConfigurationResource;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.rename.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class RenameConfigurationResourceImpl extends JOCResourceImpl implements IRenameConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenameConfigurationResourceImpl.class);

    @Override
    public JOCDefaultResponse rename(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response == null) {
                response = rename(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse rename(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);

            if (!config.getName().equalsIgnoreCase(in.getName())) {
                String newPath = (config.getFolder() + "/" + in.getName()).replaceAll("//+", "/");

                DBItemInventoryConfiguration configNewPath = dbLayer.getConfiguration(newPath, config.getType());
                if (configNewPath != null) {
                    throw new JocObjectAlreadyExistException(String.format("%s %s already exists", ConfigurationType.fromValue(config.getType())
                            .value().toLowerCase(), configNewPath.getPath()));
                }
                config.setPath(newPath);
                config.setName(in.getName());
                config.setDeployed(false);
                config.setReleased(false);
                config.setModified(Date.from(Instant.now()));
                if (!SOSString.isEmpty(config.getContent())) {
                    ConfigurationType type = ConfigurationType.fromValue(config.getType());
                    try {
                        switch (type) {
                        case JOB:
                            //obsolete: don't have path in json to update
                            break;
                        case FOLDER:
                            //obsolete: don't have path in json to update
                            // but all item recursive get a new path, folder, ...
                            throw new JocNotImplementedException("renaming of a folder is not yet implemented!");
                            //break;
                        default:
                            IConfigurationObject obj = (IConfigurationObject) Globals.objectMapper.readValue(config.getContent(), JocInventory.CLASS_MAPPING.get(type));
                            obj.setPath(config.getPath());
                            config.setContent(Globals.objectMapper.writeValueAsString(obj));
                            break;
                        }
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s]%s", config.getContent(), e.toString()), e);
                    }
                }
                session.update(config);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
