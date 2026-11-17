package com.sos.joc.classes.inventory.validators;

import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.model.common.IConfigurationObject;

public class JobResourceValidator {

    public static void validate(IConfigurationObject config) throws Exception {
        validateJobResource((JobResource) config);
    }

    public static void revalidate(IConfigurationObject config) throws Exception {
        validateJobResource((JobResource) config);
    }
    
    private static void validateJobResource(JobResource jobResource) {
        if (jobResource.getEnv() != null) {
            Validator.validateExpression("$.env", jobResource.getEnv().getAdditionalProperties());
        }
        if (jobResource.getArguments() != null) {
            Validator.validateExpression("$.arguments", jobResource.getArguments().getAdditionalProperties());
        }
    }

}
