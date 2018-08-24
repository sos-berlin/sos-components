package com.sos.webservices.order.rest.order.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.model.job.TaskHistoryFilter;
import com.sos.webservices.order.rest.order.resource.IOrdersResource;

@Path("orders")
public class OrdersResourceImpl extends JOCResourceImpl implements IOrdersResource{

    @Override
    public JOCDefaultResponse postAddOrder(String xAccessToken, String accessToken) throws Exception {
        String entity = "Hello World";
        return JOCDefaultResponse.responseStatus200(entity);
    }

}
