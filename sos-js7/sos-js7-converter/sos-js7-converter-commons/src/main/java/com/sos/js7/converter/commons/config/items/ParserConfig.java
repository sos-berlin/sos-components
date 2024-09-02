package com.sos.js7.converter.commons.config.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class ParserConfig extends AConfigItem {

    private static final String CONFIG_KEY = "parserConfig";

    private Set<String> excludedDirectoryNames;
    private Map<Integer, Set<String>> excludedDirectoryPaths; // level, paths

    public ParserConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        case "excludeddirectorynames":
            withExcludedDirectoryNames(val);
            break;
        case "excludeddirectorypaths":
            withExcludedDirectoryPaths(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return hasExcludedDirectoryNames() || hasExcludedDirectoryPaths();
    }

    public ParserConfig withExcludedDirectoryNames(String val) {
        if (!SOSString.isEmpty(val)) {
            excludedDirectoryNames = Arrays.stream(val.split(LIST_VALUE_DELIMITER)).map(e -> e.trim()).collect(Collectors.toSet());
        }
        return this;
    }

    public ParserConfig withExcludedDirectoryPaths(String val) {
        if (!SOSString.isEmpty(val)) {
            List<String> list = Arrays.stream(val.split(LIST_VALUE_DELIMITER)).map(e -> JS7ConverterHelper.normalizeDirectoryPath(e)).distinct()
                    .collect(Collectors.toList());
            excludedDirectoryPaths = new HashMap<>();
            for (String p : list) {
                int arrLen = p.split("/").length;
                if (arrLen > 0) {
                    Integer level = Integer.valueOf(arrLen - 1); // /sos/xxx/ <- level 2
                    Set<String> set = excludedDirectoryPaths.get(level);
                    if (set == null) {
                        set = new HashSet<>();
                    }
                    set.add(p);
                    excludedDirectoryPaths.put(level, set);
                }
            }
        }
        return this;
    }

    public boolean hasExcludedDirectoryNames() {
        return excludedDirectoryNames != null && excludedDirectoryNames.size() > 0;
    }

    public boolean hasExcludedDirectoryPaths() {
        return excludedDirectoryPaths != null && excludedDirectoryPaths.size() > 0;
    }

    public Set<String> getExcludedDirectoryNames() {
        return excludedDirectoryNames;
    }

    public Map<Integer, Set<String>> getExcludedDirectoryPaths() {
        return excludedDirectoryPaths;
    }

}
