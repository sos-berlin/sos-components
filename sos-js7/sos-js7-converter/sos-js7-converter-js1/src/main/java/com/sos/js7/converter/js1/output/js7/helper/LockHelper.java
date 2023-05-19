package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.sos.controller.model.lock.Lock;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.lock.LockUse;
import com.sos.js7.converter.js1.output.js7.JS12JS7Converter;

public class LockHelper {

    private static Map<String, Integer> js7Names = new HashMap<>();

    private final Path js7Path;
    private final String js7Name;
    private final Lock js7Lock;

    public LockHelper(JS12JS7Converter converter, LockUse js1LockUse) {
        this.js7Name = getUniqueJS7Name(EConfigFileExtensions.getLockName(js1LockUse.getLock().getFile()));
        this.js7Path = JS7ConverterHelper.getLockPath(converter.getJS7PathFromJS1PathParent(js1LockUse.getLock().getFile()), js7Name);
        this.js7Lock = toLock(js1LockUse.getLock());
    }

    private Lock toLock(com.sos.js7.converter.js1.common.lock.Lock js1Lock) {
        Lock l = new Lock();
        l.setTitle(js1Lock.getName());
        l.setLimit(js1Lock.getMaxNonExclusive() == null ? 1 : js1Lock.getMaxNonExclusive());
        return l;
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

    public Path getJS7Path() {
        return js7Path;
    }

    public String getJS7Name() {
        return js7Name;
    }

    public Lock getJS7Lock() {
        return js7Lock;
    }
}
