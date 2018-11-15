package com.sos.webservices.order.initiator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.util.SOSFile;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderTemplateSourceFile extends OrderTemplateSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTemplateSourceFile.class);
    protected String templateFolder;

    public OrderTemplateSourceFile(String templateFolder) {
        super();
        this.templateFolder = templateFolder;
    }

    private String getTemplateContent(File f) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
        return content;
    }
    
    

    @Override
    public List<OrderTemplate> fillListOfOrderTemplates() throws IOException {
        List<OrderTemplate> listOfOrderTemplates = new ArrayList<OrderTemplate>();
        List<File> listOfFiles = SOSFile.getFilelist(templateFolder, ".*", 0, true);
        for (File f : listOfFiles) {
            String content = getTemplateContent(f);
            OrderTemplate orderTemplate = new ObjectMapper().readValue(content, OrderTemplate.class);
            LOGGER.trace("adding order: " + orderTemplate.getOrderName() + " for workflow: " + orderTemplate.getWorkflowPath() + " on master: " + orderTemplate.getMasterId());
            if (checkMandatory(orderTemplate)){
                listOfOrderTemplates.add(orderTemplate);
            }
        }
        return listOfOrderTemplates;
    }

}
