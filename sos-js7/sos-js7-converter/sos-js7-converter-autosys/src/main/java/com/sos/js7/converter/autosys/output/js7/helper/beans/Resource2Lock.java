package com.sos.js7.converter.autosys.output.js7.helper.beans;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobResource;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.helper.PathResolver;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.beans.AJS7Object;
import com.sos.js7.converter.commons.beans.JS7Lock;

public class Resource2Lock extends AJS7Object {

    private static int DEFAULT_CAPACYTY;

    private final Path path;
    private final JS7Lock lock;

    static {
        DEFAULT_CAPACYTY = getDefaultCapacity();
    }

    public Resource2Lock(AutosysAnalyzer analyzer, CommonJobResource r) {
        String key = r.getName();

        String js7Name = JS7ConverterHelper.getJS7ObjectName(key);
        ACommonJob j = analyzer.getAllJobs().get(key);
        if (j == null) {
            this.path = Paths.get(js7Name);
        } else {
            this.path = PathResolver.getJS7ParentPath(j, js7Name).resolve(js7Name);
        }
        this.lock = new JS7Lock(js7Name, r.isExclusive() ? 1 : DEFAULT_CAPACYTY);
    }

    private static int getDefaultCapacity() {
        Integer capacity = null;
        if (Autosys2JS7Converter.CONFIG.getGenerateConfig().getLocks()) {
            capacity = Autosys2JS7Converter.CONFIG.getLockConfig().getForcedCapacity();
            if (capacity == null) {
                capacity = Autosys2JS7Converter.CONFIG.getLockConfig().getDefaultCapacity();
            }
        }
        return capacity == null ? 1 : capacity.intValue();
    }

    public JS7Lock getLock() {
        return lock;
    }

    public Path getPath() {
        return path;
    }

}
