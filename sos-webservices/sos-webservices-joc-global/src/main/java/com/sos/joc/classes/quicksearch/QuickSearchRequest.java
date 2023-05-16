package com.sos.joc.classes.quicksearch;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.model.inventory.search.RequestBaseQuickSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

public class QuickSearchRequest {

    private final String searchPrefix;
    private final String additionalProperty; //returnTypes (./inventory/quick/search) or controllerId (./workflows/quick/search)
    private long timestamp = 0;
    private List<ResponseBaseSearchItem> result;
    
    public QuickSearchRequest(String searchPrefix, List<RequestSearchReturnType> returnTypes, List<ResponseBaseSearchItem> result) {
        this.searchPrefix = normalizeSearchString(searchPrefix);
        this.additionalProperty = normalizeReturnTypes(returnTypes);
        this.result = result == null ? Collections.emptyList() : result;
        this.timestamp = Instant.now().toEpochMilli();
    }
    
    public QuickSearchRequest(String searchPrefix, String controllerId, List<ResponseBaseSearchItem> result) {
        this.searchPrefix = normalizeSearchString(searchPrefix);
        this.additionalProperty = normalizeControllerId(controllerId);
        this.result = result == null ? Collections.emptyList() : result;
        this.timestamp = Instant.now().toEpochMilli();
    }
    
    public QuickSearchRequest(String searchPrefix, List<RequestSearchReturnType> returnTypes) {
        this.searchPrefix = normalizeSearchString(searchPrefix);
        this.additionalProperty = normalizeReturnTypes(returnTypes);
    }
    
    public QuickSearchRequest(String searchPrefix, String controllerId) {
        this.searchPrefix = normalizeSearchString(searchPrefix);
        this.additionalProperty = normalizeControllerId(controllerId);
    }
    
    public String createToken(String accessToken) {
        return SOSString.hash256(searchPrefix + additionalProperty + accessToken);
    }
    
    public static String createToken(String searchPrefix, List<RequestSearchReturnType> returnTypes, String accessToken) {
        return SOSString.hash256(normalizeSearchString(searchPrefix) + normalizeReturnTypes(returnTypes) + accessToken);
    }
    
    public static String createToken(String searchPrefix, String controllerId, String accessToken) {
        return SOSString.hash256(normalizeSearchString(searchPrefix) + controllerId + accessToken);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(searchPrefix).append(additionalProperty).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof QuickSearchRequest) == false) {
            return false;
        }
        QuickSearchRequest rhs = ((QuickSearchRequest) other);
        return new EqualsBuilder().append(searchPrefix, rhs.searchPrefix).append(additionalProperty, rhs.additionalProperty).isEquals();
    }
    
    protected List<ResponseBaseSearchItem> getNewResult(RequestBaseQuickSearchFilter newSearch, List<RequestSearchReturnType> returnTypes,
            String controllerId) {
        if (newSearch == null) {
            return null;
        }
        if (newSearch.getSearch() == null) {
            return null;
        }
        String search = normalizeSearchString(newSearch.getSearch());
        if (search.length() > searchPrefix.length()) {
            search = search.substring(0, searchPrefix.length());
        }
        String additionalProp = normalizeControllerId(controllerId) + normalizeReturnTypes(returnTypes);
        if (new QuickSearchRequest(search, additionalProp).equals(this)) {
            search = normalizeSearchString(newSearch.getSearch());
            if (searchPrefix.equalsIgnoreCase(search)) {
                return result;
            }
            if (SearchStringHelper.isGlobPattern(search)) {
                String regexSearch = search.replaceAll("\\*", ".*").replaceAll("\\?", ".") + ".*";
                Predicate<String> pattern = Pattern.compile(regexSearch, Pattern.CASE_INSENSITIVE).asPredicate();
                return result.stream().filter(s -> pattern.test(s.getName())).collect(Collectors.toList());
            }
            return result.stream().filter(s -> s.getName().toLowerCase().startsWith(newSearch.getSearch())).collect(Collectors.toList());
        }
        return null;
    }
    
    private static String normalizeSearchString(final String search) {
        if (search != null) {
            return search.replaceAll("\\*\\*+", "*").replaceFirst("\\*$", "").toLowerCase();
        }
        return null;
    }
    
    private static String normalizeReturnTypes(List<RequestSearchReturnType> returnTypes) {
        if (returnTypes == null) {
            return "";
        }
        return returnTypes.stream().map(RequestSearchReturnType::ordinal).distinct().sorted().map(i -> i + "").collect(Collectors.joining());
    }
    
    private static String normalizeControllerId(String controllerId) {
        if (controllerId == null) {
            return "";
        }
        return controllerId;
    }

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    protected long getTimestamp() {
        return timestamp;
    }
    
    protected boolean isEmpty() {
        return result.isEmpty();
    }

}
