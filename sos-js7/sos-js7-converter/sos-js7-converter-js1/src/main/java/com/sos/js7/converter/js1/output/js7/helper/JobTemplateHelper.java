package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.output.js7.JS12JS7Converter;

public class JobTemplateHelper {

    private static Map<String, Integer> js7Names = new HashMap<>();

    private final JobTemplate jobTemplate;
    private final Path js7Path;
    private final String js7Name;

    public JobTemplateHelper(JS12JS7Converter converter, Path js1Job, Job js7Job) {
        this.jobTemplate = toJobTemplate(js7Job);

        this.js7Name = getUniqueJS7Name(EConfigFileExtensions.getJobName(js1Job));
        this.js7Path = JS7ConverterHelper.getJobTemplatePath(converter.getJS7PathFromJS1PathParent(js1Job), js7Name);
    }

    private JobTemplate toJobTemplate(Job j) {
        JobTemplate t = new JobTemplate();
        t.setAdmissionTimeScheme(j.getAdmissionTimeScheme());
        // t.setArguments();
        t.setCriticality(j.getCriticality());
        t.setDefaultArguments(j.getDefaultArguments());
        // t.setDescription(job.getTitle());
        t.setDocumentationName(j.getDocumentationName());
        t.setExecutable(j.getExecutable());
        t.setFailOnErrWritten(j.getFailOnErrWritten());
        t.setGraceTimeout(j.getGraceTimeout());
        t.setJobResourceNames(j.getJobResourceNames());
        t.setNotification(j.getNotification());
        t.setParallelism(j.getParallelism());
        t.setSkipIfNoAdmissionForOrderDay(j.getSkipIfNoAdmissionForOrderDay());
        t.setTimeout(j.getTimeout());
        t.setTitle(j.getTitle());
        t.setWarnIfLonger(j.getWarnIfLonger());
        t.setWarnIfShorter(j.getWarnIfShorter());
        t.setWarnOnErrWritten(j.getWarnOnErrWritten());
        if (j.getIsNotRestartable() != Boolean.TRUE) {
            t.setIsNotRestartable(null);
        }

        t.setHash(JS7ConverterHelper.getJS7JobTemplateHash(t));
        return t;
    }

    private static String getUniqueJS7Name(String js1Name) {
        String n = JS7ConverterHelper.getJS7ObjectName(js1Name);
        Integer c = js7Names.get(n);
        if (c == null) {
            js7Names.put(n, 0);
        } else {
            c = c + 1;
            js7Names.put(n, c);
            n = JS12JS7Converter.getDuplicateName(n, c);
        }
        return n;
    }

    public JobTemplate getJobTemplate() {
        return jobTemplate;
    }

    public Path getJS7Path() {
        return js7Path;
    }

    public String getJS7Name() {
        return js7Name;
    }
}
