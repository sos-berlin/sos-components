package com.sos.js7.converter.autosys.output.js7.helper.jobs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;

public class ShellJobConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellJobConverter.class);

    public static Job setExecutable(Job j, JobCMD jilJob, String platform) {
        boolean isMock = Autosys2JS7Converter.CONFIG.getMockConfig().hasForcedScript();
        boolean isUnix = platform.equals(Platform.UNIX.name());
        String commentBegin = isUnix ? "# " : "REM ";

        StringBuilder header = new StringBuilder();
        if (isMock) {
            header.append(getScriptBegin("", isUnix)).append(commentBegin).append("Mock mode").append(JS7ConverterHelper.JS7_NEW_LINE);
        }

        String command = jilJob.getCommand().getValue();

        if (header.length() == 0) {
            header.append(getScriptBegin(command, isUnix));
        }
        StringBuilder script = new StringBuilder(header);
        if (!SOSString.isEmpty(jilJob.getProfile().getValue())) {
            if (isMock) {
                script.append(commentBegin);
            }
            String commandPrefix = isUnix ? Autosys2JS7Converter.CONFIG.getJobConfig().getForcedUnixCommandPrefix() : Autosys2JS7Converter.CONFIG
                    .getJobConfig().getForcedWindowsCommandPrefix();
            if (!SOSString.isEmpty(commandPrefix)) {
                script.append(commandPrefix).append(" ");
            }
            script.append(jilJob.getProfile().getValue()).append(JS7ConverterHelper.JS7_NEW_LINE);
        }
        if (isMock) {
            script.append(commentBegin);
        }
        script.append(command);

        if (isMock) {
            script.append(JS7ConverterHelper.JS7_NEW_LINE);
            String mockScript = isUnix ? Autosys2JS7Converter.CONFIG.getMockConfig().getForcedUnixScript() : Autosys2JS7Converter.CONFIG
                    .getMockConfig().getForcedWindowsScript();
            if (!SOSString.isEmpty(mockScript)) {
                script.append(mockScript);
                script.append(JS7ConverterHelper.JS7_NEW_LINE);
            }
        }

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(Autosys2JS7Converter.CONFIG.getJobConfig().getForcedV1Compatible());
        // TODO Check
        if (jilJob.getFailCodes().getValue() != null) {
            JobReturnCode rc = new JobReturnCode();
            rc.setFailure(JS7ConverterHelper.integerListValue(jilJob.getFailCodes().getValue(), ","));
            es.setReturnCodeMeaning(rc);
        } else {
            if (jilJob.getSuccessCodes().getValue() != null) {
                if (!jilJob.getSuccessCodes().getValue().equals("0")) {
                    JobReturnCode rc = new JobReturnCode();
                    rc.setSuccess(JS7ConverterHelper.integerListValue(jilJob.getFailCodes().getValue(), ","));
                    es.setReturnCodeMeaning(rc);
                }
            } else if (jilJob.getMaxExitSuccess().getValue() != null) {
                try {
                    List<Integer> l = new ArrayList<>();
                    for (int i = 0; i <= jilJob.getMaxExitSuccess().getValue().intValue(); i++) {
                        l.add(Integer.valueOf(i));
                    }
                    if (l.size() > 0) {
                        JobReturnCode rc = new JobReturnCode();
                        rc.setSuccess(l);
                        es.setReturnCodeMeaning(rc);
                    }
                } catch (Throwable e) {
                    LOGGER.error("[" + jilJob + "][getMaxExitSuccess]" + e, e);
                }
            }
        }
        j.setExecutable(es);
        return j;
    }

    private static String getScriptBegin(String command, boolean isUnix) {
        if (isUnix) {
            if (command != null && !command.toString().startsWith("#!/")) {
                StringBuilder sb = new StringBuilder();
                if (!SOSString.isEmpty(Autosys2JS7Converter.CONFIG.getJobConfig().getDefaultUnixShebang())) {
                    sb.append(Autosys2JS7Converter.CONFIG.getJobConfig().getDefaultUnixShebang());
                    sb.append(JS7ConverterHelper.JS7_NEW_LINE);
                    return sb.toString();
                }
            }
        }
        return "";
    }

}
