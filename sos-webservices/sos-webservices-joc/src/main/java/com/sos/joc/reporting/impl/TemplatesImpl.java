package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.inventory.model.report.TemplateId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.reporting.Template;
import com.sos.joc.model.reporting.Templates;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.reporting.resource.ITemplatesResource;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class TemplatesImpl extends JOCResourceImpl implements ITemplatesResource {
    
    @Override
    public JOCDefaultResponse show(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            
            JocPermissions permission = getJocPermissions(accessToken);
            JOCDefaultResponse response = initPermissions(null, permission.getReports().getView() || permission.getInventory().getView());
            if (response != null) {
                return response;
            }
            
            Function<TemplateId, Template> mapToTemplateFromDb = templateId -> {
                Template template = new Template();
                template.setIsSupported(templateId.isSupported());
                template.setTemplateName(templateId);
                template.setTemplateId(templateId.intValue());
                template.setTitle(templateId.getTitle());
                return template;
            };
            
            Templates entity = new Templates();
            entity.setTemplates(EnumSet.allOf(TemplateId.class).stream().map(mapToTemplateFromDb).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
