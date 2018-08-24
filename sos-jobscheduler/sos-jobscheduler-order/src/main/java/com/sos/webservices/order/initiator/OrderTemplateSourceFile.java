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

public class OrderTemplateSourceFile extends OrderTemplateSource {

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
            listOfOrderTemplates.add(orderTemplate);
        }
        return listOfOrderTemplates;
    }

}
