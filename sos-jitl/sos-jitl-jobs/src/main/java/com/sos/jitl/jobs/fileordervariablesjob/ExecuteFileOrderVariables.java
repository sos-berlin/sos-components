package com.sos.jitl.jobs.fileordervariablesjob;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JobLogger;

public class ExecuteFileOrderVariables {

    private FileOrderVariablesJobArguments args;
    private Map<String, Object> jobArguments = new HashMap<>();
    private JobLogger logger;

    public ExecuteFileOrderVariables(JobLogger logger, Map<String, Object> jobArguments, FileOrderVariablesJobArguments args) {
        this.args = args;
        this.logger = logger;
        this.jobArguments = jobArguments;
    }

    public Map<String, Object> getVariables() throws Exception {

        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            BufferedReader r = null;
            r = Files.newBufferedReader(Paths.get(args.getJs7SourceFile()));

            for (String line; (line = r.readLine()) != null;) {
                if (!line.isEmpty()) {
                    String[] v = line.split("=");
                    if (v.length < 2 && !line.contains("=")) {
                        throw new SOSException(line + " in file " + args.getJs7SourceFile() + " is not a valid variable assignment <name>=<value>");
                    }
                    String vName = v[0].trim();
                    String vValue = line.substring(line.indexOf("=") + 1).trim();
                    if (jobArguments.get(vName) == null) {
                        throw new SOSException(vName + " is not a valid variable name. Couldn't find Job argument.");
                    }
                    variables.put(vName, vValue);
                }
            }

            for (Entry<String, Object> entry : jobArguments.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String s = (String) entry.getValue();

                    if (variables.get(entry.getKey()) == null && variables.get(entry.getKey()) != "js7_source_file" && !s.isEmpty()) {
                        variables.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            return variables;
        } catch (Exception e) {
            Globals.error(logger, "", e);
            throw e;
        }

    }
}
