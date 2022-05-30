package com.sos.js7.converter.js1.common;

import java.nio.file.Path;

public enum EConfigFileExtensions {

    ORDER("order", ".order.xml"), JOB_CHAIN("job_chain", ".job_chain.xml"), JOB("job", ".job.xml"), LOCK("lock", ".lock.xml"), PROCESS_CLASS(
            "process_class", ".process_class.xml"), SCHEDULE("schedule", ".schedule.xml"), MONITOR("monitor", ".monitor.xml"), JOB_CHAIN_CONFIG(
                    "config", ".config.xml");

    private String type;
    private String extension;

    private EConfigFileExtensions(String configType, String configExtension) {
        type = configType;
        extension = configExtension;
    }

    public String extension() {
        return extension;
    }

    public String type() {
        return type;
    }

    public static String getJobName(Path path) {
        return getName(JOB, path.getFileName().toString());
    }

    public static String getName(EConfigFileExtensions type, String fileName) {
        switch (type) {
        case ORDER:
            return fileName.substring(0, fileName.indexOf(","));

        case JOB_CHAIN:
        case JOB:
        case LOCK:
        case PROCESS_CLASS:
        case SCHEDULE:
        case MONITOR:
        case JOB_CHAIN_CONFIG:
            return fileName.replace(type.extension(), "");
        }
        return null;
    }
}
