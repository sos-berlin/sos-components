package com.sos.commons.hibernate.transform;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.query.TupleTransformer;

import com.sos.commons.util.SOSDate;

public class SOSNativeQueryAliasToMapTransformer<T> implements TupleTransformer<T> {

    private final boolean valueAsString;
    private final String dateTimeFormat;

    public SOSNativeQueryAliasToMapTransformer() {
        this(false, null);
    }

    public SOSNativeQueryAliasToMapTransformer(boolean valueAsString, String dateTimeFormat) {
        this.valueAsString = valueAsString;
        this.dateTimeFormat = dateTimeFormat;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T transformTuple(Object[] tuple, String[] aliases) {
        if (aliases.length == 0) {
            return null;// tuple;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>(tuple.length);
        for (int i = 0; i < tuple.length; i++) {
            String alias = aliases[i];
            if (alias != null) {
                Object origValue = tuple[i];
                if (valueAsString) {
                    String value = "";
                    if (origValue != null) {
                        value = origValue + "";
                        if (dateTimeFormat != null && origValue instanceof java.sql.Timestamp) {
                            try {
                                value = SOSDate.format(value, dateTimeFormat);
                            } catch (Exception e) {
                            }
                        }
                    }
                    result.put(alias.toLowerCase(), value);
                } else {
                    result.put(alias.toLowerCase(), origValue);
                }
            }
        }
        return (T) result;
    }

}
