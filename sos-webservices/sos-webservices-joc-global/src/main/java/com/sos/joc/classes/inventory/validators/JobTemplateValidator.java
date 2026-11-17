package com.sos.joc.classes.inventory.validators;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.common.HistoryConstants;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.exception.SOSJsonSchemaException;

public class JobTemplateValidator {

    public static void validate(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        JobTemplate jobTemplate = (JobTemplate) config;
        validateJobTemplateJob(jobTemplate, dbLayer.getScriptNames());
        // TODO something like validateOrderPreparation(workflow.getOrderPreparation());
        Validator.validateJobResourceRefs(jobTemplate.getJobResourceNames(), dbLayer);
    }

    public static void revalidate(IConfigurationObject config, Map<ConfigurationType, Set<String>> invObjNames) throws Exception {
        JobTemplate jobTemplate = (JobTemplate) config;
        validateJobTemplateJob(jobTemplate, invObjNames.getOrDefault(ConfigurationType.INCLUDESCRIPT, Collections.emptySet()));
        // TODO something like validateOrderPreparation(workflow.getOrderPreparation());
        Validator.validateJobResourceRefs(jobTemplate.getJobResourceNames(), invObjNames.getOrDefault(ConfigurationType.JOBRESOURCE, Collections
                .emptySet()));
    }

    private static void validateJobTemplateJob(JobTemplate jobTemplate, Set<String> releasedScripts) throws JsonProcessingException, IOException,
            SOSJsonSchemaException {

        switch (jobTemplate.getExecutable().getTYPE()) {
        case InternalExecutable:
            ExecutableJava ej = jobTemplate.getExecutable().cast();
            if (ej.getArguments() != null) {
                Validator.validateExpression("$.executable.arguments", ej.getArguments().getAdditionalProperties());
            }
            WorkflowValidator.checkScriptIncludes(ej.getScript(), "$.executable.script", releasedScripts);
            break;
        case ScriptExecutable:
        case ShellScriptExecutable:
            ExecutableScript es = jobTemplate.getExecutable().cast();
            if (es.getEnv() != null) {
                Validator.validateExpression("$.executable.env", es.getEnv().getAdditionalProperties());
            }
            WorkflowValidator.validateEnvironmentKeys(es.getEnv(), "$.executable.env");
            WorkflowValidator.checkReturnCodeMeaning(es, "$.");
            WorkflowValidator.checkScriptIncludes(es.getScript(), "$.executable.script", releasedScripts);
            break;
        }
        validateJobNotification(jobTemplate);
        WorkflowValidator.validateOnErrWritten(jobTemplate.getFailOnErrWritten(), jobTemplate.getWarnOnErrWritten());
    }

    private static void validateJobNotification(JobTemplate jobTemplate) throws JocConfigurationException {
        if (jobTemplate == null || jobTemplate.getNotification() == null || jobTemplate.getNotification().getMail() == null) {
            return;
        }
        if (jobTemplate.getNotification().getTypes() != null && jobTemplate.getNotification().getTypes().size() > 0) {
            // to is required, when cc or bcc defined and the mail is not suppressed
            if (SOSString.isEmpty(jobTemplate.getNotification().getMail().getTo())) {
                if (!SOSString.isEmpty(jobTemplate.getNotification().getMail().getCc()) || !SOSString.isEmpty(jobTemplate.getNotification().getMail()
                        .getBcc())) {
                    throw new JocConfigurationException("$.notification.mail: missing \"to\"");
                }
            }
        }
        try {
            String content = Globals.objectMapper.writeValueAsString(jobTemplate.getNotification());
            if (content.length() > HistoryConstants.MAX_LEN_NOTIFICATION) {
                throw new JocConfigurationException(String.format(
                        "$.notification: reduce recipients, max json length %s (current length=%s) exceeded", HistoryConstants.MAX_LEN_NOTIFICATION,
                        content.length()));
            }

        } catch (JsonProcessingException e) {
            //
        }
    }
}
