package com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl;

import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.InternalExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.db.DBJobConverter;
import com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.rest.RESTJobConverter;
import com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.yade.YADEJobConverter;

public class JITLJobConverter {

    public static final String DEFAULT_USER = "js7_converter_user";
    public static final String DEFAULT_PASSWORD = "js7_converter_password";

    public static void clear() {
        DBJobConverter.clear();
        RESTJobConverter.clear();
        YADEJobConverter.clear();
    }

    public static Job createExecutable(Job j, String className) {
        ExecutableJava ex = new ExecutableJava();
        ex.setInternalType(InternalExecutableType.JITL);
        ex.setClassName(className);

        Environment env = new Environment();
        if (Autosys2JS7Converter.CONFIG.getMockConfig().hasForcedScript()) {
            env.getAdditionalProperties().put("mock_level", "INFO");
        }

        ex.setArguments(env);
        j.setExecutable(ex);
        return j;
    }

    public static void addArgument(Job j, String name, String value) {
        ((ExecutableJava) j.getExecutable()).getArguments().getAdditionalProperties().put(name, value);
    }
}
