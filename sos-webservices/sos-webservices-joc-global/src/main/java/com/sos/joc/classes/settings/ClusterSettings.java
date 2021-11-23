package com.sos.joc.classes.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc.ShowViewName;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsUser;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.ShowViewProperties;
import com.sos.joc.model.SuffixPrefix;


public class ClusterSettings {
    
    private static Logger LOGGER = LoggerFactory.getLogger(ClusterSettings.class);
    
    public static SuffixPrefix getCopyPasteSuffixPrefix(ConfigurationGlobalsJoc settings) {
        return getSuffixPrefix(settings.getCopyPasteSuffix(), settings.getCopyPastePrefix());
    }
    
    public static SuffixPrefix getRestoreSuffixPrefix(ConfigurationGlobalsJoc settings) {
        return getSuffixPrefix(settings.getRestoreSuffix(), settings.getRestorePrefix());
    }
    
    public static SuffixPrefix getImportSuffixPrefix(ConfigurationGlobalsJoc settings) {
        return getSuffixPrefix(settings.getImportSuffix(), settings.getImportPrefix());
    }
    
    public static String getDefaultProfileAccount(ConfigurationGlobalsJoc settings) {
        String account = settings.getDefaultProfileAccount().getValue();
        if (account == null) {  // for tests 
            return settings.getDefaultProfileAccount().getDefault();
        }
        return account;
    }
    
    public static List<String> getCommentsForAuditLog(ConfigurationGlobalsJoc settings) {
        String comments = settings.getCommentsForAuditLog().getValue();
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(comments.split(";"));
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
        Map<ShowViewName, Boolean> showViews = settings.getShowViews();

        if (withLogging) {
            logShowViewSettings(showViews).ifPresent(msg -> LOGGER.info(msg));
        }

        ShowViewProperties svProp = new ShowViewProperties();
        svProp.setAuditLog(showViews.get(ShowViewName.auditlog));
        svProp.setConfiguration(showViews.get(ShowViewName.configuration));
        svProp.setDailyPlan(showViews.get(ShowViewName.dailyplan));
        svProp.setDashboard(showViews.get(ShowViewName.dashboard));
        svProp.setHistory(showViews.get(ShowViewName.history));
        svProp.setResources(showViews.get(ShowViewName.resources));
        svProp.setWorkflows(showViews.get(ShowViewName.workflows));
        svProp.setFileTransfers(showViews.get(ShowViewName.filetransfer));
        //svProp.setJobStreams(showViews.get(ShowViewName.jobstreams));
        return svProp;
    }
    
    public static String getJOCPwd(ConfigurationGlobalsJoc settings) {
        String pwd = settings.getJOCPwd().getValue();
        if (pwd == null || pwd.isEmpty()) {
           return settings.getJOCPwd().getDefault();
        }
        return pwd;
    }
    
    public static String getHistoryPwd(ConfigurationGlobalsJoc settings) {
        String pwd = settings.getHistoryPwd().getValue();
        if (pwd == null || pwd.isEmpty()) {
           return settings.getHistoryPwd().getDefault();
        }
        return pwd;
    }
    
    public static boolean getWelcomeDoNotRemindMe(ConfigurationGlobalsUser settings) {
        String welcome = settings.getWelcomeDoNotRemindMe().getValue();
        return welcome != null && welcome.equalsIgnoreCase("true");
    }
    
    public static boolean getWelcomeGotIt(ConfigurationGlobalsUser settings) {
        String welcome = settings.getWelcomeGotIt().getValue();
        return welcome != null && welcome.equalsIgnoreCase("true");
    }
    
    private static SuffixPrefix getSuffixPrefix(ConfigurationEntry suf, ConfigurationEntry pref) {
        String suffix = suf.getValue();
        String prefix = pref.getValue();
        if (suffix == null || suffix.isEmpty()) {
            suffix = suf.getDefault();
        } else {
            suffix = trimSuffix(suffix);
        }
        if (prefix == null || prefix.isEmpty()) {
            prefix = pref.getDefault();
        } else {
            prefix = trimPrefix(prefix);
        }
        SuffixPrefix sp = new SuffixPrefix();
        sp.setPrefix(prefix);
        sp.setSuffix(suffix);
        return sp;
    }
    
    private static String trimSuffix(String suffix) {
        return suffix.trim().replaceFirst("^-+", "");
    }
    
    private static String trimPrefix(String prefix) {
        return prefix.trim().replaceFirst("-+$", "");
    }
    
    private static Optional<String> logShowViewSettings(Map<ShowViewName, Boolean> showViews) {
        Map<Boolean, Set<String>> m = showViews.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.groupingBy(
                Map.Entry::getValue, Collectors.mapping(e -> e.getKey().name(), Collectors.toSet())));
        m.putIfAbsent(Boolean.TRUE, Collections.emptySet());
        m.putIfAbsent(Boolean.FALSE, Collections.emptySet());
        boolean hiddenViewsNotEmpty = !m.get(Boolean.FALSE).isEmpty();
        boolean shownViewsNotEmpty = !m.get(Boolean.TRUE).isEmpty();
        if (hiddenViewsNotEmpty || shownViewsNotEmpty) {
            StringBuilder msg = new StringBuilder("Views ");
            if (hiddenViewsNotEmpty) {
                msg.append(m.get(Boolean.FALSE).toString()).append(" are hidden");
            }
            if (hiddenViewsNotEmpty && shownViewsNotEmpty) {
                msg.append(" and ");
            }
            if (shownViewsNotEmpty) {
                msg.append(m.get(Boolean.TRUE).toString()).append(" are shown");
            }
            msg.append(" because of JOC settings that ignore permissions.");
            return Optional.of(msg.toString());
        }
        return Optional.empty();
    }
}
