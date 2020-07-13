package com.sos.js7.order.initiator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.js7.order.initiator.model.OrderTemplate;

public class OrderTemplateSourceFile extends OrderTemplateSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplateSourceFile.class);
    protected String templateFolder;

    public OrderTemplateSourceFile(String templateFolder) {
        super();
        this.templateFolder = templateFolder;
    }

    
    @Override
    public List<OrderTemplate> fillListOfOrderTemplates() throws IOException {
        List<OrderTemplate> listOfOrderTemplates = new ArrayList<OrderTemplate>();
        
         for (Path p : Files.walk(Paths.get(templateFolder)).filter(p -> !Files.isDirectory(p)).collect(Collectors.toSet())) {
            OrderTemplate orderTemplate = new ObjectMapper().readValue(Files.readAllBytes(p), OrderTemplate.class);
            if (orderTemplate.getPlan_order_automatically() == null){
                orderTemplate.setPlan_order_automatically(true);
            }
            if (orderTemplate.getSubmit_order_to_controller_when_planned() == null){
                orderTemplate.setSubmit_order_to_controller_when_planned(true);
            }
            
            LOGGER.trace("adding order: " + orderTemplate.getOrderTemplateName() + " for workflow: " + orderTemplate.getWorkflowPath() + " on controller: "
                    + orderTemplate.getJobschedulerId());
            if (checkMandatory(orderTemplate)) {
                listOfOrderTemplates.add(orderTemplate);
            }
        }
        return listOfOrderTemplates;
    }

}
