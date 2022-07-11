package com.sos.joc.inventory.impl;

import java.util.Date;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReleasablesRecall;
import com.sos.joc.model.inventory.release.ReleasableRecallFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class ReleasablesRecallImpl extends JOCResourceImpl implements IReleasablesRecall {

    private static final String API_CALL = "./inventory/releasables/recall";

    @Override
    public JOCDefaultResponse postRecall(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, ReleasableRecallFilter.class);
            ReleasableRecallFilter recallFilter = Globals.objectMapper.readValue(filter, ReleasableRecallFilter.class);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            recallFilter.getReleasables().stream().map(released -> dbLayer.getReleasedConfiguration(released.getName(), released.getObjectType()))
                    .map(dbItemReleased -> {
                        dbLayer.recallReleasedConfiguration(dbItemReleased);
                        return dbItemReleased.getFolder();
                    }).distinct().forEach(folder -> JocInventory.postEvent(folder));
            return JOCDefaultResponse.responseStatusJSOk(new Date());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
