package com.sos.joc.orders.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.orders.resource.IOrdersResourceHistory;

@Path("orders")
public class OrdersResourceHistoryImpl extends JOCResourceImpl implements IOrdersResourceHistory {

	private static final String API_CALL = "./orders/history";

	@Override
	public JOCDefaultResponse postOrdersHistory(String xAccessToken, String accessToken, OrdersFilter ordersFilter)
			throws Exception {
		return postOrdersHistory(getAccessToken(xAccessToken, accessToken), ordersFilter);
	}

	public JOCDefaultResponse postOrdersHistory(String accessToken, OrdersFilter ordersFilter) throws Exception {

		try {
			if (ordersFilter.getJobschedulerId() == null) {
				ordersFilter.setJobschedulerId("");
			}
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, accessToken,
					ordersFilter.getJobschedulerId(),
					getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), accessToken).getHistory().getView()
							.isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			OrderHistory entity = new OrderHistory();
			entity.setDeliveryDate(new Date());
			//entity.setHistory(null);

			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

}
