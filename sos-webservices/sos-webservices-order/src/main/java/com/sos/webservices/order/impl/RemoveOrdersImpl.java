package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlannedOrders;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.webservices.order.classes.OrderHelper;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.webservices.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IRemoveOrderResource;

@Path("orders")
public class RemoveOrdersImpl extends JOCResourceImpl implements IRemoveOrderResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrdersImpl.class);
	private static final String API_CALL = "./orders/removeOrders";

	private void removeOrders(OrdersFilter ordersFilter)
			throws JocConfigurationException, DBConnectionRefusedException, JobSchedulerInvalidResponseDataException,
			JsonProcessingException, SOSException, URISyntaxException, DBOpenSessionException {
		SOSHibernateSession sosHibernateSession = null;

		try {
			sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
			DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);

			if (ordersFilter.getOrders().size() > 0) {
				FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
				filter.setListOfOrders(ordersFilter.getOrders());
				filter.setJobSchedulerId(ordersFilter.getJobschedulerId());
				List<DBItemDailyPlannedOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
				OrderHelper orderHelper = new OrderHelper();
				String answer = orderHelper.removeFromJobSchedulerMaster(ordersFilter.getJobschedulerId(),
						listOfPlannedOrders);
				dbLayerDailyPlannedOrders.delete(filter);
			}
			if (ordersFilter.getDateFrom() != null && ordersFilter.getDateTo() != null) {

				Date fromDate = null;
				Date toDate = null;

				FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
				filter.setJobSchedulerId(ordersFilter.getJobschedulerId());
				fromDate = JobSchedulerDate.getDateFrom(ordersFilter.getDateFrom(), ordersFilter.getTimeZone());
				filter.setPlannedStartFrom(fromDate);
				toDate = JobSchedulerDate.getDateTo(ordersFilter.getDateTo(), ordersFilter.getTimeZone());
				filter.setPlannedStartTo(toDate);
				List<DBItemDailyPlannedOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
				OrderHelper orderHelper = new OrderHelper();
				String answer = orderHelper.removeFromJobSchedulerMaster(ordersFilter.getJobschedulerId(),
						listOfPlannedOrders);
				dbLayerDailyPlannedOrders.delete(filter);
			}

			// TODO: Check answers for error

			Globals.commit(sosHibernateSession);
		} finally {
			Globals.disconnect(sosHibernateSession);
		}
	}

	@Override
	public JOCDefaultResponse postRemoveOrders(String xAccessToken, OrdersFilter ordersFilter) throws JocException {
		LOGGER.debug("Reading the daily plan");
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, xAccessToken,
					ordersFilter.getJobschedulerId(),
					getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView()
							.isStatus());

			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			removeOrders(ordersFilter);
			return JOCDefaultResponse.responseStatusJSOk(new Date());

		} catch (JocException e) {
			LOGGER.error(getJocError().getMessage(), e);
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(getJocError().getMessage(), e);
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

}
