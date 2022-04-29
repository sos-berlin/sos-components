package com.sos.js7.converter.autosys.common.v12;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobFT;
import com.sos.js7.converter.autosys.common.v12.job.JobFW;
import com.sos.js7.converter.autosys.common.v12.job.JobNotSupported;
import com.sos.js7.converter.autosys.common.v12.job.JobOMTF;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeInclude;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

public class JobParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobParser.class);

    private static Map<ConverterJobType, List<Method>> JOB_METHODS = null;
    private static Map<String, List<Method>> JOB_INCLUDES_METHODS = null;

    public JobParser() {
        init();
    }

    public ACommonJob parse(Properties p) {
        ACommonJob job = newJob(getConverterJobType(p.getProperty("job_type")));
        return set(job, p);
    }

    private void init() {
        if (JOB_METHODS == null) {
            setJobMethods();
        }
        if (JOB_INCLUDES_METHODS == null) {
            setJobIncludesMethods();
        }
    }

    private ConverterJobType getConverterJobType(String val) {
        ConverterJobType t = ConverterJobType.CMD;
        if (val != null && val.trim().length() > 0) {
            try {
                t = ConverterJobType.valueOf(val.toUpperCase());
            } catch (Throwable e) {
                t = null;
            }
            if (t == null) {
                t = ConverterJobType.NOT_SUPPORTED;
            }
        }
        return t;
    }

    private ACommonJob newJob(ConverterJobType type) {
        switch (type) {
        case CMD:
            return new JobCMD();
        case FT:
            return new JobFT();
        case FW:
            return new JobFW();
        case BOX:
            return new JobBOX();
        case OMTF:
            return new JobOMTF();
        default:
            return new JobNotSupported();
        }
    }

    private List<Method> getJobMethods(Class<?> clazz) {
        List<Method> m = SOSReflection.getAllDeclaredMethods(clazz);
        return m.stream().filter(e -> {
            return e.isAnnotationPresent(JobAttributeSetter.class);
        }).collect(Collectors.toList());
    }

    private void setJobMethods() {
        JOB_METHODS = new HashMap<>();
        JOB_METHODS.put(ConverterJobType.BOX, getJobMethods(JobBOX.class));
        JOB_METHODS.put(ConverterJobType.CMD, getJobMethods(JobCMD.class));
        JOB_METHODS.put(ConverterJobType.FT, getJobMethods(JobFT.class));
        JOB_METHODS.put(ConverterJobType.FW, getJobMethods(JobFW.class));
        JOB_METHODS.put(ConverterJobType.NOT_SUPPORTED, getJobMethods(JobNotSupported.class));
        JOB_METHODS.put(ConverterJobType.OMTF, getJobMethods(JobOMTF.class));
    }

    private void setJobIncludesMethods() {
        List<Field> fields = SOSReflection.getAllDeclaredFields(ACommonJob.class);
        fields = fields.stream().filter(e -> {
            return e.isAnnotationPresent(JobAttributeInclude.class);
        }).collect(Collectors.toList());

        JOB_INCLUDES_METHODS = new HashMap<>();
        for (Field f : fields) {
            f.setAccessible(true);
            JobAttributeInclude a = f.getAnnotation(JobAttributeInclude.class);
            JOB_INCLUDES_METHODS.put(a.getMethod(), getJobMethods(f.getType()));
        }
    }

    private ACommonJob set(ACommonJob job, Properties p) throws IllegalArgumentException {
        List<Method> jobMethods = JOB_METHODS.get(job.getConverterJobType());
        p.forEach((name, value) -> {
            boolean found = false;
            if (!set(job, jobMethods, (String) name, (String) value, null)) {
                x: for (Map.Entry<String, List<Method>> entry : JOB_INCLUDES_METHODS.entrySet()) {
                    if (set(job, entry.getValue(), (String) name, (String) value, entry.getKey())) {
                        found = true;
                        break x;
                    }
                }
            } else {
                found = true;
            }
            if (!found) {
                SOSArgument<String> ua = new SOSArgument<>((String) name, false);
                ua.setValue((String) value);
                job.getUnknown().add(ua);
            }
        });

        return job;
    }

    private boolean set(ACommonJob job, List<Method> methods, String name, String value, String includeGetMethod) {
        for (Method m : methods) {
            JobAttributeSetter a = m.getAnnotation(JobAttributeSetter.class);
            if (name.equals(a.name())) {
                m.setAccessible(true);
                try {
                    if (includeGetMethod == null) {
                        // e.g. job.setJobType(...)
                        m.invoke(job, value);
                    } else {
                        // e.g. job.getFolder().setApplication(...)
                        Method im = job.getClass().getMethod(includeGetMethod);
                        m.invoke(im.invoke(job), value);
                    }
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s.%s][can't invoke method]method=%s,value=%s", getClass().getName(), m.getName(), value, e
                            .toString()), e);
                }
                return true;
            }
        }
        return false;
    }

}
