package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.orders.Orders;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.jobChain.JobChainsFilter;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";

    @Override
    public JOCDefaultResponse postOrdersOverviewSnapshot(String xAccessToken, String accessToken, JobChainsFilter jobChainsFilter) throws Exception {
        return postOrdersOverviewSnapshot(getAccessToken(xAccessToken, accessToken), jobChainsFilter);
    }

	public JOCDefaultResponse postOrdersOverviewSnapshot(String accessToken, JobChainsFilter jobChainsFilter)
			throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobChainsFilter, accessToken,
					jobChainsFilter.getJobschedulerId(),
					getPermissonsJocCockpit(jobChainsFilter.getJobschedulerId(), accessToken).getOrder().getView()
							.isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

            boolean withFolderFilter = jobChainsFilter.getFolders() != null && !jobChainsFilter.getFolders().isEmpty();
            List<Folder> folders = addPermittedFolder(jobChainsFilter.getFolders());
            OrdersSnapshot entity = new OrdersSnapshot();
            
            if (withFolderFilter && (folders == null || folders.isEmpty())) {
                entity.setOrders(null);
                entity.setSurveyDate(Date.from(Instant.now()));
                entity.setDeliveryDate(entity.getSurveyDate());
            } else {
                jobChainsFilter.setFolders(folders);
                entity = Orders.getSnapshot(new JOCJsonCommand(this), accessToken, jobChainsFilter);
            }

            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JobSchedulerConnectionResetException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }
}
