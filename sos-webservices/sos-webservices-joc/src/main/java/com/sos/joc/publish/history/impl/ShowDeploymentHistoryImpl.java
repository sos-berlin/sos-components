package com.sos.joc.publish.history.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.publish.DepHistory;
import com.sos.joc.model.publish.DepHistoryItem;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.history.resource.IShowDeploymentHistory;
import com.sos.schema.JsonValidator;

@Path("inventory/deployment")
public class ShowDeploymentHistoryImpl extends JOCResourceImpl implements IShowDeploymentHistory {

    private static final String API_CALL = "./inventory/deployment/history";

    @Override
    public JOCDefaultResponse postShowDeploymentHistory(String xAccessToken, byte[] showDepHistoryFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, showDepHistoryFilter, xAccessToken);
            JsonValidator.validateFailFast(showDepHistoryFilter, ShowDepHistoryFilter.class);
            ShowDepHistoryFilter filter = Globals.objectMapper.readValue(showDepHistoryFilter, ShowDepHistoryFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getPermissonsJocCockpit(null, xAccessToken).getHistory().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemDeploymentHistory> dbHistoryItems = dbLayer.getDeploymentHistory(filter);
            return JOCDefaultResponse.responseStatus200(getDepHistoryFromDBItems(dbHistoryItems));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
             if (hibernateSession != null) {
                 hibernateSession.close();
             }
        }
    }
    
    private DepHistory getDepHistoryFromDBItems(List<DBItemDeploymentHistory> dbHistoryItems) {
        DepHistory depHistory = new DepHistory();
        depHistory.setDepHistory(dbHistoryItems.stream().map(item -> mapDBItemToDepHistoryItem(item)).collect(Collectors.toList()));
        depHistory.setDeliveryDate(Date.from(Instant.now()));
        return depHistory;
    }

    private DepHistoryItem mapDBItemToDepHistoryItem (DBItemDeploymentHistory dbItem) {
        DepHistoryItem depHistoryItem = new DepHistoryItem();
        depHistoryItem.setAccount(dbItem.getAccount());
        depHistoryItem.setCommitId(dbItem.getCommitId());
        depHistoryItem.setControllerId(dbItem.getControllerId());
        depHistoryItem.setDeleteDate(dbItem.getDeleteDate());
        depHistoryItem.setDeploymentDate(dbItem.getDeploymentDate());
        depHistoryItem.setDeploymentId(dbItem.getId());
        depHistoryItem.setDeployType(DeployType.fromValue(dbItem.getType()).value());
        depHistoryItem.setFolder(dbItem.getFolder());
        depHistoryItem.setInvConfigurationId(dbItem.getInventoryConfigurationId());
        depHistoryItem.setOperation(OperationType.fromValue(dbItem.getOperation()).name());
        depHistoryItem.setPath(dbItem.getPath());
        depHistoryItem.setState(DeploymentState.fromValue(dbItem.getState()).name());
        depHistoryItem.setVersion(dbItem.getVersion());
        return depHistoryItem;
        
    }

}
