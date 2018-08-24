package com.sos.webservices.order.initiator;

import java.io.IOException;
import java.util.List;

import com.sos.webservices.order.initiator.model.OrderTemplate;

public abstract class OrderTemplateSource {
    public abstract List <OrderTemplate> fillListOfOrderTemplates() throws IOException;

}
