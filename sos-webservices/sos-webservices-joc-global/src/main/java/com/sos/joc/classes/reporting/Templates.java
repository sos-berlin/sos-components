package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.report.TemplateId;
import com.sos.joc.Globals;
import com.sos.joc.db.reporting.DBItemReportTemplate;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.reporting.Template;

public class Templates extends AReporting {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Templates.class);
    private static final Path templatesDir = reportingDir.resolve("app/templates");
    
    public static List<Template> getTemplates() throws IOException {
        
        Function<TemplateId, Template> mapToTemplateFromFile = templateId -> {
            Path templateFile = templatesDir.resolve("template_" + templateId.intValue() + ".json");
            Template template = new Template();
            if (templateId.isSupported()) {
                if (Files.exists(templateFile)) {
                    try {
                        template = Globals.objectMapper.readValue(Files.readAllBytes(templateFile), Template.class);
                    } catch (Exception e) {
                        LOGGER.error("", e);
                        return null;
                    }
                } else {
                    LOGGER.warn(templateFile.toString() + " doesn't exist");
                    return null;
                }
            }
            template.setIsSupported(templateId.isSupported());
            template.setTemplateName(templateId);
            template.setTemplateId(templateId.intValue());
            return template;
        };
        
        return EnumSet.allOf(TemplateId.class).stream().map(mapToTemplateFromFile).filter(Objects::nonNull).collect(Collectors.toList());
        
    }

    public static List<Template> getTemplates_() {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("GetTemplates");
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            Map<Integer, byte[]> dbTemplates = dbLayer.getTemplates().stream().collect(Collectors.toMap(DBItemReportTemplate::getTemplateId,
                    DBItemReportTemplate::getContent));
            
            return EnumSet.allOf(TemplateId.class).stream().map(templateId -> {
                Template template = new Template();
                if (dbTemplates.containsKey(templateId.intValue())) {
                    try {
                        template = Globals.objectMapper.readValue(dbTemplates.get(templateId.intValue()), Template.class);
                    } catch (Exception e) {
                        // throw
                        return null;
                    }
                }
                template.setIsSupported(templateId.isSupported());
                template.setTemplateName(templateId);
                template.setTemplateId(templateId.intValue());
                return template;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            
//            return dbLayer.getTemplates().stream().map(t -> {
//                try {
//                    TemplateId templateId = TemplateId.fromValue(t.getTemplateId());
//                    Template template = Globals.objectMapper.readValue(t.getContent(), Template.class);
//                    template.setIsSupported(templateId.isSupported());
//                    template.setTemplateId(t.getTemplateId());
//                    template.setTemplateName(templateId);
//                    return template;
//                } catch (Exception e) {
//                    // template is missing in Enum
//                    return null;
//                }
//            }).filter(Objects::nonNull).collect(Collectors.toList());

        } finally {
            Globals.disconnect(session);
        }
    }

    public static void updateTemplates() {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("UpdateTemplates");
            Set<Path> templates = Files.list(templatesDir).filter(f -> f.getFileName().toString().matches("template_\\d+\\.json")).collect(Collectors
                    .toSet());
            Date now = Date.from(Instant.now());
            for (Path template : templates) {
                updateTemplate(template, session, now);
                Files.deleteIfExists(template);
            }
        } catch (Exception e) {
            //
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static void updateTemplate(Path template, SOSHibernateSession session, Date now) throws SOSHibernateException, IOException {
        DBItemReportTemplate item = null;
        Integer templateId = Integer.valueOf(template.getFileName().toString().replaceAll("\\D", ""));
        item = session.get(DBItemReportTemplate.class, templateId);
        boolean newItem = item == null;
        if (newItem) {
            item = new DBItemReportTemplate();
            item.setTemplateId(templateId);
        }
        item.setContent(Files.readAllBytes(template));
        item.setCreated(now);
        if (newItem) {
            session.save(item);
        } else {
            session.update(item);
        }
    }
    
    public static void storeTemplateInTmpDir(TemplateId tId) throws IOException, SOSHibernateException {

        Files.write(tmpDir.resolve("template_" + tId.intValue() + ".json"), getTemplate(tId));
    }
    
    private static byte[] getTemplate(TemplateId tId) throws SOSHibernateException {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("StoreTemplate");
            DBItemReportTemplate item = session.get(DBItemReportTemplate.class, tId.intValue());
            Globals.disconnect(session);
            if (item == null) {
                if (tId.isSupported()) {
                    throw new DBMissingDataException("Couldn't find template " + tId.value());
                } else {
                    throw new DBMissingDataException("Template " + tId.value() + " is not longer supported.");
                }
            }
            return item.getContent();
        } finally {
            Globals.disconnect(session);
        }
    }
}
