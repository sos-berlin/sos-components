package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.model.order.OrdersV;
import com.sos.joc.yade.resource.IYadeOrdersResource;

@Path("yade")
public class YadeOrdersResourceImpl extends JOCResourceImpl implements IYadeOrdersResource {

	private static final String API_CALL = "./yade/orders";

	@Override
	public JOCDefaultResponse postOrders(String accessToken, OrdersFilter ordersBody) throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersBody, accessToken,
					ordersBody.getJobschedulerId(), getPermissonsJocCockpit(ordersBody.getJobschedulerId(), accessToken)
							.getOrder().getView().isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			OrdersV entity = new OrdersV();
			entity.setOrders(null);
			entity.setDeliveryDate(Date.from(Instant.now()));

			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}
}
