package com.sos.joc.classes.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        SuffixPrefix sp = new SuffixPrefix();
        sp.setSuffix(settings.getCopyPasteSuffix().getValue());
        sp.setPrefix("");
        if (sp.getSuffix() == null || sp.getSuffix().isEmpty()) {
            sp.setSuffix("");
            sp.setPrefix(settings.getCopyPastePrefix().getValue());
            if (sp.getPrefix() == null || sp.getPrefix().isEmpty()) {
                sp.setPrefix("");
                sp.setSuffix(settings.getCopyPasteSuffix().getDefault());
            } else {
                sp.setPrefix(trimPrefix(sp.getPrefix())); 
            }
        } else {
            sp.setSuffix(trimSuffix(sp.getSuffix())); 
        }
        return sp;
    }
    
    public static SuffixPrefix getRestoreSuffixPrefix(ConfigurationGlobalsJoc settings) {
        SuffixPrefix sp = new SuffixPrefix();
        sp.setSuffix(settings.getRestoreSuffix().getValue());
        sp.setPrefix("");
        if (sp.getSuffix() == null || sp.getSuffix().isEmpty()) {
            sp.setSuffix("");
            sp.setPrefix(settings.getRestorePrefix().getValue());
            if (sp.getPrefix() == null || sp.getPrefix().isEmpty()) {
                sp.setPrefix("");
                sp.setSuffix(settings.getRestoreSuffix().getDefault());
            } else {
                sp.setPrefix(trimPrefix(sp.getPrefix())); 
            }
        } else {
            sp.setSuffix(trimSuffix(sp.getSuffix())); 
        }
        return sp;
    }
    
    public static String getDefaultProfileAccount(ConfigurationGlobalsJoc settings) {
        return getValueOrDefault(settings.getDefaultProfileAccount());
    }
    
    public static List<String> getCommentsForAuditLog(ConfigurationGlobalsJoc settings) {
        return Arrays.asList(getValueOrDefault(settings.getCommentsForAuditLog()).split(";"));
    }
    
    public static boolean getForceCommentsForAuditLog(ConfigurationGlobalsJoc settings) {
        String force = getValueOrDefault(settings.getForceCommentsForAuditLog());
        return force != null && force.equalsIgnoreCase("true");
    }
    
    public static boolean getEnableRememberMe(ConfigurationGlobalsJoc settings) {
        String rememberMe = getValueOrDefault(settings.getEnableRememberMe());
        return rememberMe != null && rememberMe.equalsIgnoreCase("true");
    }
    
    public static ShowViewProperties getShowViews(ConfigurationGlobalsJoc settings) {
        return getShowViews(settings, false);
    }
    
    public static ShowViewProperties getShowViews(ConfigurationGlobalsJoc settings, boolean withLogging) {
        Map<String, Boolean> showViews = new HashMap<>();
        showViews.put(settings.getShowViewAuditlog().getName(), getBoolean(getValueOrDefault(settings.getShowViewAuditlog())));
        showViews.put(settings.getShowViewConfiguration().getName(), getBoolean(getValueOrDefault(settings.getShowViewConfiguration())));
        showViews.put(settings.getShowViewDailyplan().getName(), getBoolean(getValueOrDefault(settings.getShowViewDailyplan())));
        showViews.put(settings.getShowViewDashboard().getName(), getBoolean(getValueOrDefault(settings.getShowViewDashboard())));
        showViews.put(settings.getShowViewHistory().getName(), getBoolean(getValueOrDefault(settings.getShowViewHistory())));
        showViews.put(settings.getShowViewResources().getName(), getBoolean(getValueOrDefault(settings.getShowViewResources())));
        showViews.put(settings.getShowViewWorkflows().getName(), getBoolean(getValueOrDefault(settings.getShowViewWorkflows())));
        
        if (withLogging) {
            String msg = logShowViewSettings(showViews);
            if (msg != null) {
                LOGGER.info(msg);
            }
        }
        
        ShowViewProperties svProp = new ShowViewProperties();
        svProp.setAuditLog(showViews.get(settings.getShowViewAuditlog().getName()));
        svProp.setConfiguration(showViews.get(settings.getShowViewConfiguration().getName()));
        svProp.setDailyPlan(showViews.get(settings.getShowViewDailyplan().getName()));
        svProp.setDashboard(showViews.get(settings.getShowViewDashboard().getName()));
        svProp.setHistory(showViews.get(settings.getShowViewHistory().getName()));
        svProp.setResources(getBoolean(getValueOrDefault(settings.getShowViewResources())));
        svProp.setWorkflows(getBoolean(getValueOrDefault(settings.getShowViewWorkflows())));
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
    
    private static String getValueOrDefault(ConfigurationEntry entry) {
        String v = entry.getValue();
        if (v == null || v.isEmpty()) {
            v = entry.getDefault(); 
        }
        if (v != null) {
            return v.trim();
        }
        return v;
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
    
    private static String logShowViewSettings(Map<String, Boolean> showViews) {
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
            msg.append(" because of ./joc.properties settings");
            return msg.toString();
        }
        return null;
    }
}
