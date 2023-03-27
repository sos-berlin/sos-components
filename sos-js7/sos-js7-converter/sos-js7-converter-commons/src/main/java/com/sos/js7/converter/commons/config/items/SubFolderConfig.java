package com.sos.js7.converter.commons.config.items;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubFolderConfig extends AConfigItem {

    private static final String CONFIG_KEY = "subFolderConfig";

    private Map<String, Integer> mappings;
    private String separator = "_";

    public SubFolderConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key) {
        case "mappings":
            withMappings(val);
            break;
        case "separator":
            withSeparator(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return mappings == null || mappings.size() == 0;
    }

    /** Sub Folder mapping - extract job names parts(parts are separated by subFolderSeparator) to create a sub folders<br/>
     * Example 1<br>
     * - input map: aapg = 2; ebzc = 0; wmad = 0<br/>
     * - input jil job name (application aapg): xxx_yyy_zzz_my_job<br/>
     * - output: zzz/my_job ("zzz" has the index 2 if the job name is separated by "_")
     * 
     * @param map
     * @return */
    public SubFolderConfig withMappings(String mapping) {
        Map<String, Integer> map = new HashMap<>();
        if (mapping != null) {
            // map and remove duplicates
            map = Stream.of(mapping.trim().split(LIST_VALUE_DELIMITER)).map(e -> e.split("=")).filter(e -> e.length == 2).collect(Collectors.toMap(
                    arr -> arr[0].trim(), arr -> Integer.parseInt(arr[1].trim()), (oldValue, newValue) -> oldValue));
        }
        return withMappings(map);
    }

    public SubFolderConfig withMappings(Map<String, Integer> val) {
        this.mappings = val;
        return this;
    }

    public SubFolderConfig withSeparator(String val) {
        this.separator = val;
        return this;
    }

    public Map<String, Integer> getMappings() {
        if (mappings == null) {
            mappings = new HashMap<>();
        }
        return mappings;
    }

    public String getSeparator() {
        return separator;
    }

}
