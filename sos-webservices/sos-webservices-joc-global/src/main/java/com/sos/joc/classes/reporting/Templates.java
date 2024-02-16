package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.model.reporting.Template;

public class Templates extends AReporting {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Templates.class);
    
    public static List<Template> getTemplates() throws IOException {
        
        return Files.list(reportingDir.resolve("bin/templates")).map(f -> {
            try {
                return Globals.objectMapper.readValue(Files.readAllBytes(f), Template.class);
            } catch (Exception e) {
                //
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        
    }
}
