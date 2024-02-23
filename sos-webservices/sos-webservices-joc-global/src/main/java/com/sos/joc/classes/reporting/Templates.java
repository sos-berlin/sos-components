package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.reporting.DBItemReportTemplate;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.model.reporting.Template;

public class Templates extends AReporting {
    
    public static List<Template> getTemplates() throws IOException {
        
        return Files.list(reportingDir.resolve("app/templates")).map(f -> {
            try {
                return Globals.objectMapper.readValue(Files.readAllBytes(f), Template.class);
            } catch (Exception e) {
                //
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Template> getTemplates_() {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("GetTemplates");
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            return dbLayer.getTemplates().stream().map(t -> {
                try {
                    return Globals.objectMapper.readValue(t.getContent(), Template.class);
                } catch (Exception e) {
                    //
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

        } finally {
            Globals.disconnect(session);
        }
    }

    public static void updateTemplates() {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("UpdateTemplates");
            Set<Path> templates = Files.list(reportingDir.resolve("bin/templates")).filter(f -> f.getFileName().toString().matches(
                    "template_.*\\.json")).collect(Collectors.toSet());
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
}
