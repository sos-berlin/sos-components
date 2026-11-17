package com.sos.joc.classes.inventory.validators;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;

public class FileOrderSourceValidator {

    public static void validate(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        FileOrderSource fileOrderSource = (FileOrderSource) config;
        String position = "$.workflowName";
        String json = Validator.validateWorkflowRef(fileOrderSource.getWorkflowName(), dbLayer, position);
        Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
        validateFileVariable(w.getOrderPreparation(), fileOrderSource.getWorkflowName(), position);
        validateFileOrderSourceDirAndPattern(fileOrderSource);
        Validator.validateTimeZone(fileOrderSource.getTimeZone());
    }

    public static void revalidate(IConfigurationObject config, Map<String, String> workflowJsonsByName) throws Exception {
        String position = "$.workflowName";
        FileOrderSource fileOrderSource = (FileOrderSource) config;
        String json = Validator.validateWorkflowRef(fileOrderSource.getWorkflowName(), workflowJsonsByName, position);
        Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
        validateFileVariable(w.getOrderPreparation(), fileOrderSource.getWorkflowName(), position);
        validateFileOrderSourceDirAndPattern(fileOrderSource);
        Validator.validateTimeZone(fileOrderSource.getTimeZone());
    }

    private static void validateFileVariable(Requirements r, String workflowName, String position) throws JocConfigurationException {
        if (r == null || r.getParameters() == null || r.getParameters().getAdditionalProperties() == null || !r.getParameters()
                .getAdditionalProperties().containsKey("file")) {
            throw new JocConfigurationException(String.format("%s: the 'file' variable is missing in the referenced workflow '%s'", position,
                    workflowName));
        }
    }

    private static void validateFileOrderSourceDirAndPattern(FileOrderSource fileOrderSource) throws JocConfigurationException {
        if (fileOrderSource.getDirectoryExpr() != null) {
            Validator.validateExpression("$.directoryExpr: ", fileOrderSource.getDirectoryExpr());
        }
        if (fileOrderSource.getPattern() != null) {
            try {
                Pattern.compile(fileOrderSource.getPattern());
            } catch (PatternSyntaxException e) {
                throw new JocConfigurationException("$.pattern: " + e.getMessage());
            }
        }
        Validator.testJavaNameRulesAtTags("$.tags", fileOrderSource.getTags());
    }
}
