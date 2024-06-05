package com.sos.joc.classes.quicksearch;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.inventory.InventorySearchDBLayer;
import com.sos.joc.db.inventory.items.InventoryQuickSearchItem;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.model.common.DeployedObjectQuickSearchFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.RequestBaseQuickSearchFilter;
import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;
import com.sos.joc.model.inventory.search.ResponseQuickSearch;

public class QuickSearchStore {
    
    private static QuickSearchStore INSTANCE;
    private volatile ConcurrentMap<String, QuickSearchRequest> results = new ConcurrentHashMap<>();
    private Timer timer;
    private TimerTask timerTask;
    private final static long cleanupPeriodInMillis = TimeUnit.SECONDS.toMillis(10);
    
    private QuickSearchStore() {
        startTimer();
    }
    
    private static QuickSearchStore getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QuickSearchStore();
        }
        return INSTANCE;
    }
    
    public static void close() {
        getInstance()._close();
    }
    
    public static RequestQuickSearchFilter checkToken(RequestQuickSearchFilter request, String accessToken) {
        return getInstance()._checkToken(request, accessToken);
    }
    
    public static DeployedObjectQuickSearchFilter checkToken(DeployedObjectQuickSearchFilter request, String returnType, String accessToken) {
        return getInstance()._checkToken(request, returnType, accessToken);
    }
    
    public static void putResult(String token, RequestQuickSearchFilter request, List<ResponseBaseSearchItem> result) {
        getInstance()._putResult(token, request, result);
    }
    
    public static void putResult(String token, DeployedObjectQuickSearchFilter request, String returnType, List<ResponseBaseSearchItem> result) {
        getInstance()._putResult(token, request, returnType, result);
    }
    
    public static void putResult(String token, QuickSearchRequest result) {
        getInstance()._putResult(token, result);
    }
    
    public static void updateTimeStamp(String token, long timestamp) {
        getInstance()._updateTimeStamp(token, timestamp);
    }
    
    public static void updateTimeStamp(String token) {
        getInstance()._updateTimeStamp(token, Instant.now().toEpochMilli());
    }
    
    public static List<ResponseBaseSearchItem> getResult(RequestQuickSearchFilter request) {
        return getInstance()._getResult(request, request.getReturnTypes(), null);
    }
    
    public static List<ResponseBaseSearchItem> getResult(DeployedObjectQuickSearchFilter request) {
        return getInstance()._getResult(request, null, request.getControllerId());
    }
    
    public static void deleteResult(String token) {
        getInstance()._deleteResult(token);
    }
    
    public static ResponseQuickSearch getAnswer(RequestQuickSearchFilter in, final String accessToken,
            final SOSAuthFolderPermissions folderPermissions, boolean forInventory) throws SOSHibernateException {
        return getAnswer(in, accessToken, folderPermissions, forInventory, null);
    }
    
    public static ResponseQuickSearch getAnswer(RequestQuickSearchFilter in, final String accessToken,
            final SOSAuthFolderPermissions folderPermissions, boolean forInventory, String controllerId) throws SOSHibernateException {
        ResponseQuickSearch answer = new ResponseQuickSearch();

        if (!in.getQuit()) {
            in = checkToken(in, accessToken);
            answer.setResults(getBasicReleasedOrInventoryObjectsSearch(in, folderPermissions, forInventory, controllerId));
        } else {
            answer.setResults(Collections.emptyList());
        }
        
        Instant now = Instant.now();
        answer.setDeliveryDate(Date.from(now));
        
        if (!in.getQuit()) {
            if (in.getToken() != null) {
                answer.setToken(in.getToken());
                updateTimeStamp(in.getToken(), now.toEpochMilli());
            } else {
                QuickSearchRequest result = new QuickSearchRequest(in.getSearch(), in.getReturnTypes(), answer.getResults());
                answer.setToken(result.createToken(accessToken));
                putResult(answer.getToken(), result);
            }
        } else {
            deleteResult(in.getToken());
        }
        return answer;
    }
    
    public static ResponseQuickSearch getAnswer(DeployedObjectQuickSearchFilter in, final ConfigurationType type, final String accessToken,
            final SOSAuthFolderPermissions folderPermissions) throws SOSHibernateException {
        ResponseQuickSearch answer = new ResponseQuickSearch();

        if (!in.getQuit()) {
            in = checkToken(in, type.value(), accessToken);
            answer.setResults(getBasicDeployedObjectsSearch(in, type, folderPermissions));
        } else {
            answer.setResults(Collections.emptyList());
        }

        Instant now = Instant.now();
        answer.setDeliveryDate(Date.from(now));

        if (!in.getQuit()) {
            if (in.getToken() != null) {
                answer.setToken(in.getToken());
                updateTimeStamp(in.getToken(), now.toEpochMilli());
            } else {
                QuickSearchRequest result = new QuickSearchRequest(in.getSearch(), in.getControllerId(), type.value(), answer.getResults());
                answer.setToken(result.createToken(accessToken));
                putResult(answer.getToken(), result);
            }
        } else {
            deleteResult(in.getToken());
        }
        return answer;
    }
    
    public static ResponseQuickSearch getTagsAnswer(DeployedObjectQuickSearchFilter in, final ConfigurationType type, final String accessToken,
            final SOSAuthFolderPermissions folderPermissions) throws SOSHibernateException {
        return getTagsAnswer(in, type, null, accessToken, folderPermissions);
    }

    public static ResponseQuickSearch getOrderTagsAnswer(DeployedObjectQuickSearchFilter in, final String accessToken) throws SOSHibernateException {
        return getTagsAnswer(in, null, "ORDER", accessToken, null);
    }
    
    public static ResponseQuickSearch getTagsAnswer(DeployedObjectQuickSearchFilter in, final ConfigurationType type,
            final String nonConfigurationType, final String accessToken, final SOSAuthFolderPermissions folderPermissions)
            throws SOSHibernateException {
        ResponseQuickSearch answer = new ResponseQuickSearch();
        
        String returnType = type != null ? type.value() : nonConfigurationType;
        returnType += "TAGS";

        if (!in.getQuit()) {
            in = checkToken(in, returnType, accessToken);
            answer.setResults(getBasicTagsSearch(in, type, nonConfigurationType, folderPermissions));
        } else {
            answer.setResults(Collections.emptyList());
        }

        Instant now = Instant.now();
        answer.setDeliveryDate(Date.from(now));

        if (!in.getQuit()) {
            if (in.getToken() != null) {
                answer.setToken(in.getToken());
                updateTimeStamp(in.getToken(), now.toEpochMilli());
            } else {
                QuickSearchRequest result = new QuickSearchRequest(in.getSearch(), in.getControllerId(), returnType, answer.getResults());
                answer.setToken(result.createToken(accessToken));
                putResult(answer.getToken(), result);
            }
        } else {
            deleteResult(in.getToken());
        }
        return answer;
    }
    
    private RequestQuickSearchFilter _checkToken(RequestQuickSearchFilter request, String accessToken) {
        if (request == null) {
            return null;
        }
        if (request.getToken() != null) {
            if (!results.containsKey(request.getToken())) {
                request.setToken(null);
            }
        } else {
            String token = QuickSearchRequest.createToken(request.getSearch(), request.getReturnTypes(), accessToken);
            if (results.containsKey(token)) {
                request.setToken(token);
            }
        }
        return request;
    }
    
    private DeployedObjectQuickSearchFilter _checkToken(DeployedObjectQuickSearchFilter request, String returnType, String accessToken) {
        if (request == null) {
            return null;
        }
        if (request.getToken() != null) {
            if (!results.containsKey(request.getToken())) {
                request.setToken(null);
            }
        } else {
            String token = QuickSearchRequest.createToken(request.getSearch(), request.getControllerId(), returnType, accessToken);
            if (results.containsKey(token)) {
                request.setToken(token);
            }
        }
        return request;
    }
    
    private void _putResult(String token, RequestQuickSearchFilter request, List<ResponseBaseSearchItem> result) {
        if (result != null) {
            results.put(token, new QuickSearchRequest(request.getSearch(), request.getReturnTypes(), result));
        }
    }
    
    private void _putResult(String token, DeployedObjectQuickSearchFilter request, String returnType, List<ResponseBaseSearchItem> result) {
        if (result != null) {
            results.put(token, new QuickSearchRequest(request.getSearch(), request.getControllerId(), returnType, result));
        }
    }
    
    private void _putResult(String token, QuickSearchRequest result) {
        if (result != null && !result.isEmpty() ) {
            results.put(token, result);
        }
    }
    
    private void _updateTimeStamp(String token, long timestamp) {
        QuickSearchRequest result = results.get(token);
        if (result != null) {
            result.setTimestamp(timestamp);
        }
    }
    
    private List<ResponseBaseSearchItem> _getResult(RequestBaseQuickSearchFilter request, List<RequestSearchReturnType> returnTypes,
            String controllerId) {
        if (request != null && request.getToken() != null) {
            QuickSearchRequest result = results.get(request.getToken());
            if (result != null) {
                List<ResponseBaseSearchItem> newResult =  result.getNewResult(request, returnTypes, controllerId);
                if (newResult == null) {
                    results.remove(request.getToken());
                    request.setToken(null);
                    return null;
                }
                return newResult;
            }
        }
        return null;
    }
    
    private void _deleteResult(String token) {
        if (token != null) {
            results.remove(token);
        }
    }
    
    private void _close() {
        try {
            if (timerTask != null) {
                timerTask.cancel(); 
            }
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        } catch (Throwable e) {
            //
        }
        results.clear();
    }
    
    private void startTimer() {
        if (timer == null) {
            timer = new Timer("Timer-CleanupQuickSearch");
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    Long oldTimestamp = Instant.now().toEpochMilli() - cleanupPeriodInMillis - cleanupPeriodInMillis;
                    results.values().removeIf(e -> e.getTimestamp() < oldTimestamp);
                }

            };
            timer.scheduleAtFixedRate(timerTask, cleanupPeriodInMillis, cleanupPeriodInMillis);
        }
    }
    
    private static List<ResponseBaseSearchItem> getBasicReleasedOrInventoryObjectsSearch(RequestQuickSearchFilter in,
            final SOSAuthFolderPermissions folderPermissions, boolean forInventory, String controllerId) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {

            if (in.getToken() != null) {
                List<ResponseBaseSearchItem> result = getResult(in);
                if (result != null) {
                    return result;
                } else {
                    // obsolete token
                    in.setToken(null);
                }
            }

            session = Globals.createSosHibernateStatelessConnection("QuickSearch");
            List<InventoryQuickSearchItem> items = null;
            Stream<InventoryQuickSearchItem> itemsStream = null;
            
            if (forInventory) {
                InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
                items = dbLayer.getQuickSearchInventoryConfigurations(in.getReturnTypes(), in.getSearch());
            } else {

                if (in.getReturnTypes() == null) {
                    DocumentationDBLayer dbLayer = new DocumentationDBLayer(session);
                    items = dbLayer.getQuickSearchDocus(in.getSearch());
                } else if (in.getReturnTypes().get(0).equals(RequestSearchReturnType.SCHEDULE)) {
                    // get deployed WorkflowNames of controllerId
                    DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
                    dbFilter.setControllerId(controllerId);
                    dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
                    DeployedConfigurationDBLayer depDbLayer = new DeployedConfigurationDBLayer(session);
                    Collection<String> workflowNames = depDbLayer.getDeployedNames(dbFilter).getOrDefault(DeployType.WORKFLOW.intValue(), Collections
                            .emptyMap()).values();

                    InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
                    itemsStream = dbLayer.getQuickSearchReleasedSchedulesWithDeployedWorkflows(controllerId, in.getSearch(), workflowNames);
                } else {
                    InventorySearchDBLayer dbLayer = new InventorySearchDBLayer(session);
                    items = dbLayer.getQuickSearchReleasedConfigurations(in.getReturnTypes(), in.getSearch());
                }
            }
            
            if (items != null) {
                itemsStream = items.stream();
            }

            if (itemsStream != null) {
                Predicate<InventoryQuickSearchItem> isPermitted = item -> folderPermissions.isPermittedForFolder(item.getFolder());
                Comparator<InventoryQuickSearchItem> comp = Comparator.comparing(InventoryQuickSearchItem::getLowerCasePath);
                // if (in.getReturnType() == null) {
                // comp = comp.thenComparingInt(i -> i.getObjectType() == null ? 99 : i.getObjectType().intValue());
                // }
                Stream<InventoryQuickSearchItem> stream = itemsStream.filter(isPermitted);
                if (!forInventory) {
                    stream = stream.peek(item -> item.setObjectType(null));
                }
                return stream.sorted(comp).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static List<ResponseBaseSearchItem> getBasicDeployedObjectsSearch(DeployedObjectQuickSearchFilter in, final ConfigurationType type,
            final SOSAuthFolderPermissions folderPermissions) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {

            if (in.getToken() != null) {
                List<ResponseBaseSearchItem> result = getResult(in);
                if (result != null) {
                    return result;
                } else {
                    // obsolete token
                    in.setToken(null);
                }
            }

            session = Globals.createSosHibernateStatelessConnection("QuickSearch");
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);

            List<InventoryQuickSearchItem> items = dbLayer.getQuickSearchInventoryConfigurations(in.getControllerId(), Collections.singleton(type
                    .intValue()), in.getSearch());

            if (items != null) {
                Predicate<InventoryQuickSearchItem> isPermitted = item -> folderPermissions.isPermittedForFolder(item.getFolder());
                Comparator<InventoryQuickSearchItem> comp = Comparator.comparing(InventoryQuickSearchItem::getLowerCasePath);
                // if (in.getReturnType() == null) {
                // comp = comp.thenComparingInt(i -> i.getObjectType() == null ? 99 : i.getObjectType().intValue());
                // }
                return items.stream().filter(isPermitted).peek(item -> item.setObjectType(null)).sorted(comp).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static List<ResponseBaseSearchItem> getBasicTagsSearch(DeployedObjectQuickSearchFilter in, final ConfigurationType type,
            final String nonConfigurationType, final SOSAuthFolderPermissions folderPermissions) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {

            if (in.getToken() != null) {
                List<ResponseBaseSearchItem> result = getResult(in);
                if (result != null) {
                    return result;
                } else {
                    // obsolete token
                    in.setToken(null);
                }
            }

            session = Globals.createSosHibernateStatelessConnection("TagSearch");
            Comparator<ResponseBaseSearchItem> comp = Comparator.comparingInt(ResponseBaseSearchItem::getOrdering);

            if (type != null) {
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
                List<InventoryTagItem> items = dbLayer.getTagSearch(in.getControllerId(), Collections.singleton(type.intValue()), in.getSearch());

                if (items != null) {
                    Predicate<InventoryTagItem> isPermitted = item -> folderPermissions.isPermittedForFolder(item.getFolder());
                    return items.stream().filter(isPermitted).peek(item -> item.setFolder(null)).distinct().sorted(comp).collect(Collectors.toList());
                } else {
                    return Collections.emptyList();
                }

            } else if (nonConfigurationType != null && nonConfigurationType.equals("ORDER")) {
                return OrderTags.getTagSearch(in.getControllerId(), in.getSearch(), session).stream().distinct().sorted(comp).collect(Collectors
                        .toList());
            }
            return Collections.emptyList();
        } finally {
            Globals.disconnect(session);
        }
    }

}
