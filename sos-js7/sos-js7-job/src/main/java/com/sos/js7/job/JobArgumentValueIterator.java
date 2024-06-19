package com.sos.js7.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.sos.commons.util.SOSString;
import com.sos.js7.job.JobArgument.ArgumentFlatType;

public class JobArgumentValueIterator implements Iterator<Object> {

    private final JobArgument<?> argument;
    private final String prefix;

    private final List<Object> filteredValues = new ArrayList<>();
    private final boolean isListValueSingltonMap;

    private boolean isTypeMap = false;
    private int currentIndex = 0;

    protected JobArgumentValueIterator(JobArgument<?> argument, String prefix) {
        this.argument = argument;
        this.prefix = prefix;
        this.isListValueSingltonMap = argument.getArgumentFlatType().equals(ArgumentFlatType.LIST_VALUE_SINGLTON_MAP);
        initValues();
    }

    private void initValues() {
        switch (argument.getArgumentType()) {
        case FLAT:
            if (filterByPrefix(argument.getValue())) {
                filteredValues.add(argument.getValue());
            }
            break;
        case LIST:
            if (isListValueSingltonMap) {
                int i = 0;
                for (Object item : (List<?>) argument.getValue()) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                        if (filterByPrefix(entry.getValue())) {
                            filteredValues.add(new SingltonMap(i, entry));
                            i++;
                        }
                    }
                }
            } else {
                for (Object item : (List<?>) argument.getValue()) {
                    if (filterByPrefix(item)) {
                        filteredValues.add(item);
                    }
                }
            }
            break;
        case MAP:
            isTypeMap = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) argument.getValue()).entrySet()) {
                if (filterByPrefix(entry.getValue())) {
                    filteredValues.add(entry);
                }
            }
            break;
        case SET:
            for (Object item : (Set<?>) argument.getValue()) {
                if (filterByPrefix(item)) {
                    filteredValues.add(item);
                }
            }
            break;
        default:
            throw new IllegalStateException("Unsupported argument type: " + argument.getArgumentType());
        }

    }

    private boolean filterByPrefix(Object value) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        return value.toString().startsWith(prefix);
    }

    @Override
    public boolean hasNext() {
        return currentIndex < filteredValues.size();
    }

    /** @apiNote Returns an object because if the prefix is ​​not used, the current object representation is returned. */
    @Override
    public Object next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Object o = filteredValues.get(currentIndex++);
        if (o == null) {
            return null;
        }
        if (isTypeMap) {
            return ((Map.Entry<?, ?>) o).getValue();
        } else if (isListValueSingltonMap) {
            return ((SingltonMap) o).value;
        }
        return o;
    }

    /** @return the next element in the iteration as a string without the specified prefix. */
    public String nextWithoutPrefix() {
        return SOSString.removePrefix(next(), prefix);
    }

    public Object current() {
        try {
            return filteredValues.get(currentIndex - 1);
        } catch (Throwable e) {
            return null;
        }
    }

    public String getArgumentName() {
        return argument.getName();
    }

    @SuppressWarnings("unchecked")
    public void set(Object newValue) throws Exception {
        if (currentIndex <= 0 || currentIndex > filteredValues.size()) {
            throw new IllegalStateException();
        }
        Object newValueConverted = JobArgument.convertFlatValue(argument, newValue);
        Object currentValue = filteredValues.get(currentIndex - 1);
        switch (argument.getArgumentType()) {
        case FLAT:
            argument.applyValue(newValueConverted);
            break;
        case LIST:
            List<Object> list = (List<Object>) argument.getValue();
            if (isListValueSingltonMap) {
                SingltonMap sm = (SingltonMap) currentValue;
                Map<String, Object> map = (Map<String, Object>) list.get(sm.listIndex);
                if (map != null && map.containsKey(sm.key)) {
                    map.put(sm.key, newValueConverted);
                }
            } else {
                int index = list.indexOf(currentValue);
                if (index != -1) {
                    list.set(index, newValueConverted);
                }
            }
            break;
        case MAP:
            Map<String, Object> map = (Map<String, Object>) argument.getValue();
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) currentValue;
            map.put(entry.getKey(), newValueConverted);
            break;
        case SET:
            Set<Object> set = (Set<Object>) argument.getValue();
            set.remove(currentValue);
            set.add(newValueConverted);
            break;
        default:
            throw new IllegalStateException("Unsupported argument type: " + argument.getArgumentType());
        }
        filteredValues.set(currentIndex - 1, newValueConverted);
    }

    private class SingltonMap {

        private final int listIndex;
        private final String key;
        private final Object value;

        private SingltonMap(int listIndex, Map.Entry<?, ?> entry) {
            this.listIndex = listIndex;
            this.key = entry.getKey().toString();
            this.value = entry.getValue();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("listIndex=").append(listIndex);
            sb.append(",key=").append(key);
            sb.append(",value=").append(value);
            return sb.toString();
        }
    }

}
