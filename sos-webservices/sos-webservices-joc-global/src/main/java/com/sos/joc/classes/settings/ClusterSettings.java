package com.sos.joc.classes.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.ShowViewProperties;
import com.sos.joc.model.SuffixPrefix;


public class ClusterSettings {
    
    private static Logger LOGGER = LoggerFactory.getLogger(ClusterSettings.class);
    
    public static SuffixPrefix getCopyPasteSuffixPrefix(ConfigurationGlobalsJoc settings) {
        String suffix = "";
        String prefix = "";
        if (!suffixPrefixIsDefault(settings.getCopyPasteSuffix())) {
            suffix = settings.getCopyPasteSuffix().getValue();
        }
        if (suffix.isEmpty()) {
            if (!suffixPrefixIsDefault(settings.getCopyPastePrefix())) {
                prefix = settings.getCopyPastePrefix().getValue();
            }
            if (prefix.isEmpty()) {
                suffix = settings.getCopyPasteSuffix().getDefault();
            } else {
                prefix = trimPrefix(prefix);
            }
        } else {
            suffix = trimSuffix(suffix);
        }
        SuffixPrefix sp = new SuffixPrefix();
        sp.setPrefix(prefix);
        sp.setSuffix(suffix);
        return sp;
    }
    
    public static SuffixPrefix getRestoreSuffixPrefix(ConfigurationGlobalsJoc settings) {
        String suffix = "";
        String prefix = "";
        if (!suffixPrefixIsDefault(settings.getRestoreSuffix())) {
            suffix = settings.getRestoreSuffix().getValue();
        }
        if (suffix.isEmpty()) {
            if (!suffixPrefixIsDefault(settings.getRestorePrefix())) {
                prefix = settings.getRestorePrefix().getValue();
            }
            if (prefix.isEmpty()) {
                suffix = settings.getRestoreSuffix().getDefault();
            } else {
                prefix = trimPrefix(prefix);
            }
        } else {
            suffix = trimSuffix(suffix);
        }
        SuffixPrefix sp = new SuffixPrefix();
        sp.setPrefix(prefix);
        sp.setSuffix(suffix);
        return sp;
    }
    
    public static String getDefaultProfileAccount(ConfigurationGlobalsJoc settings) {
        return settings.getDefaultProfileAccount().getValue();
    }
    
    public static List<String> getCommentsForAuditLog(ConfigurationGlobalsJoc settings) {
        return Arrays.asList(settings.getCommentsForAuditLog().getValue().split(";"));
    }
    
    public static boolean getForceCommentsForAuditLog(ConfigurationGlobalsJoc settings) {
        String force = settings.getForceCommentsForAuditLog().getValue();
        return force != null && force.equalsIgnoreCase("true");
    }
    
    public static boolean getEnableRememberMe(ConfigurationGlobalsJoc settings) {
        String rememberMe = settings.getEnableRememberMe().getValue();
        return rememberMe != null && rememberMe.equalsIgnoreCase("true");
    }
    
    public static ShowViewProperties getShowViews(ConfigurationGlobalsJoc settings) {
        return getShowViews(settings, false);
    }
    
    public static ShowViewProperties getShowViews(ConfigurationGlobalsJoc settings, boolean withLogging) {
        Map<String, Boolean> showViews = new HashMap<>();
        showViews.put(settings.getShowViewAuditlog().getName(), getBoolean(settings.getShowViewAuditlog().getValue()));
        showViews.put(settings.getShowViewConfiguration().getName(), getBoolean(settings.getShowViewConfiguration().getValue()));
        showViews.put(settings.getShowViewDailyplan().getName(), getBoolean(settings.getShowViewDailyplan().getValue()));
        showViews.put(settings.getShowViewDashboard().getName(), getBoolean(settings.getShowViewDashboard().getValue()));
        showViews.put(settings.getShowViewHistory().getName(), getBoolean(settings.getShowViewHistory().getValue()));
        showViews.put(settings.getShowViewResources().getName(), getBoolean(settings.getShowViewResources().getValue()));
        showViews.put(settings.getShowViewWorkflows().getName(), getBoolean(settings.getShowViewWorkflows().getValue()));
        
        if (withLogging) {
            logShowViewSettings(showViews).ifPresent(msg -> LOGGER.info(msg));
        }
        
        ShowViewProperties svProp = new ShowViewProperties();
        svProp.setAuditLog(showViews.get(settings.getShowViewAuditlog().getName()));
        svProp.setConfiguration(showViews.get(settings.getShowViewConfiguration().getName()));
        svProp.setDailyPlan(showViews.get(settings.getShowViewDailyplan().getName()));
        svProp.setDashboard(showViews.get(settings.getShowViewDashboard().getName()));
        svProp.setHistory(showViews.get(settings.getShowViewHistory().getName()));
        svProp.setResources(showViews.get(settings.getShowViewResources().getName()));
        svProp.setWorkflows(showViews.get(settings.getShowViewWorkflows().getName()));
        //svProp.setFileTransfers(fileTransfers);
        //svProp.setJobStreams(jobStreams);
        return svProp;
    }
    
    private static String trimSuffix(String suffix) {
        return suffix.trim().replaceFirst("^-+", "");
    }
    
    private static String trimPrefix(String prefix) {
        return prefix.trim().replaceFirst("-+$", "");
    }
    
    private static boolean suffixPrefixIsDefault(ConfigurationEntry suffixPrefix) {
        return suffixPrefix.getValue().equals(suffixPrefix.getDefault());
    }
    
    private static Boolean getBoolean(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        } else if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
    
    private static Optional<String> logShowViewSettings(Map<String, Boolean> showViews) {
        Map<Boolean, Set<String>> m = showViews.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.groupingBy(
                Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
        m.putIfAbsent(Boolean.TRUE, Collections.emptySet());
        m.putIfAbsent(Boolean.FALSE, Collections.emptySet());
        StringBuilder msg = new StringBuilder();
        msg.append("Views ");
        if (!m.get(Boolean.FALSE).isEmpty()) {
            msg.append(m.get(Boolean.FALSE).toString()).append(" are hidden");
        }
        if (!m.get(Boolean.FALSE).isEmpty() && !m.get(Boolean.TRUE).isEmpty()) {
            msg.append(" and ");
        }
        if (!m.get(Boolean.TRUE).isEmpty()) {
            msg.append(m.get(Boolean.TRUE).toString()).append(" are shown");
        }
        if (!m.get(Boolean.FALSE).isEmpty() || !m.get(Boolean.TRUE).isEmpty()) {
            msg.append(" because of JOC settings");
            return Optional.of(msg.toString());
        }
        return Optional.empty();
    }
}
