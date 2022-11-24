package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.jobchain.JobChain;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;

public class ConverterObjects {

    private ConverterObject<StandaloneJob> standalone = new ConverterObject<>();
    private ConverterObject<JobChain> jobChains = new ConverterObject<>();
    private ConverterObject<ProcessClass> processClasses = new ConverterObject<>();
    private ConverterObject<Path> files = new ConverterObject<>();

    public ConverterObject<StandaloneJob> getStandalone() {
        return standalone;
    }

    public ConverterObject<JobChain> getJobChains() {
        return jobChains;
    }

    public ConverterObject<ProcessClass> getProcessClasses() {
        return processClasses;
    }

    public ConverterObject<Path> getFiles() {
        return files;
    }

    public class ConverterObject<T> {

        private Map<String, T> unique = new HashMap<>();
        private Map<String, List<T>> duplicates = new HashMap<>();

        public Map<String, T> getUnique() {
            return unique;
        }

        public Map<String, List<T>> getDuplicates() {
            return duplicates;
        }
    }
}
