package com.sos.joc;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthCurrentAccountsList;
import com.sos.auth.sosintern.SOSInternAuthSession;
import com.sos.auth.sosintern.classes.SOSInternAuthSubject;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.JocWebserviceDataContainer;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.security.configuration.permissions.ControllerPermissions;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;

/** For testing web services in the IDE<br/>
 * Suitable for "simple" web services that use e.g. a database connection but not the Proxy/Events etc.<br/>
 * Current state:<br/>
 * - Multithreading<br/>
 * -- Any number of web service calls can be executed in parallel<br/>
 * - Login<br/>
 * -- Mock login as JOC root user with all View permissions<br/>
 * -- TODO: use an existing login<br/>
 * - Initializing<br/>
 * -- Globals Hibernate Factory, reading Globals Configuration Sections(dailyplan, joc etc)<br/>
 * -- TODO Initializing Proxy and other components (see see com.sos.joc.servlet.JocServletContainer init)<br/>
 * --- Note: the JOC Cluster is intentionally left uninitialized to avoid starting services(history,etc.)<br/>
 */
public class UnitTestSimpleWSImplHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestSimpleWSImplHelper.class);

    private final Class<?> clazz;
    private final JOCResourceImpl instance;

    private final Path propertiesFile;
    private Path hibernateConfigurationFile;

    private List<String> controllerIds;

    private List<CompletableFuture<JOCDefaultResponse>> futures = new ArrayList<>();

    public UnitTestSimpleWSImplHelper(JOCResourceImpl instance) throws Exception {
        this(instance, null, null);
    }

    public UnitTestSimpleWSImplHelper(JOCResourceImpl instance, Path propertiesFile) throws Exception {
        this(instance, propertiesFile, null);
    }

    public UnitTestSimpleWSImplHelper(JOCResourceImpl instance, Path propertiesFile, Path hibernateConfigurationFile) throws Exception {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
        this.instance = instance;
        this.clazz = instance.getClass();
        this.propertiesFile = propertiesFile;
        this.hibernateConfigurationFile = hibernateConfigurationFile;
    }

    /** see com.sos.joc.servlet.JocServletContainer */
    public void init() {
        // System Properties
        Globals.setSystemProperties();

        initJOCProperties();
        initConfigurationGlobals();
        setControllerIds();
        // Globals.readUnmodifiables();

        // TODO Proxy etc
    }

    public void destroy() {
        waitForAllTasksToComplete();
        Globals.closeFactory();
    }

    private void initConfigurationGlobals() {
        Globals.setConfigurationGlobals(new ConfigurationGlobals());
        DBLayerJocCluster dbLayer = null;
        try {
            dbLayer = new DBLayerJocCluster(Globals.getHibernateFactory().openStatelessSession("initConfigurationGlobals"));

            // dbLayer.beginTransaction();
            DBItemJocConfiguration item = dbLayer.getGlobalsSettings();
            // dbLayer.commit();
            dbLayer.close();
            dbLayer = null;

            if (item == null) {
                LOGGER.warn("[initConfigurationGlobals][not found]defaults are used...");
            } else {
                Globals.getConfigurationGlobals().setConfigurationValues(Globals.objectMapper.readValue(item.getConfigurationItem(),
                        GlobalSettings.class));
                LOGGER.info("[initConfigurationGlobals]executed");
            }

        } catch (Throwable e) {
            LOGGER.warn(String.format("[initConfigurationGlobals]%s", e.toString()), e);
            if (dbLayer != null) {
                // dbLayer.rollback();
            }
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private void setControllerIds() {
        SOSHibernateSession session = null;
        try {
            session = Globals.getHibernateFactory().openStatelessSession("getControllersId");
            Query<String> query = session.createQuery("select controllerId from " + DBLayer.DBITEM_INV_JS_INSTANCES);
            controllerIds = session.getResultList(query);
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getControllersId]%s", e.toString()), e);
        } finally {
            Globals.getHibernateFactory().close(session);
        }
    }

    private void initJOCProperties() {
        // JOC Properties
        if (propertiesFile == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
            if (hibernateConfigurationFile == null) {
                setPropertyHibernateConfigurationFile(Paths.get("src/test/resources/hibernate.cfg.xml"));
            } else {
                setPropertyHibernateConfigurationFile(hibernateConfigurationFile);
            }
        } else {
            Globals.sosCockpitProperties = new JocCockpitProperties(propertiesFile.toAbsolutePath());
            if (hibernateConfigurationFile != null) {
                setPropertyHibernateConfigurationFile(hibernateConfigurationFile);
            }
        }
    }

    private void setPropertyHibernateConfigurationFile(Path file) {
        Globals.sosCockpitProperties.getProperties().put("hibernate_configuration_file", file.toAbsolutePath().toString());
    }

    private String mockJOCLoginAsRoot() {
        Globals.jocWebserviceDataContainer = JocWebserviceDataContainer.getInstance();
        Globals.jocWebserviceDataContainer.setCurrentAccountsList(new SOSAuthCurrentAccountsList());

        SOSAuthCurrentAccount a = new SOSAuthCurrentAccount("root");
        a.setAccessToken("JOC", "tmp"); // avoid NPE
        a.setRoles(Collections.singleton("all"));
        a.setIsApprover(true);

        mockRootJOCPermissions(a);

        Globals.jocWebserviceDataContainer.getCurrentAccountsList().addAccount(a);

        SOSInternAuthSubject as = new SOSInternAuthSubject();
        as.setAccessToken(a.getAccessToken());
        as.setAuthenticated(true);
        as.setIsForcePasswordChange(false);
        as.setAuthenticated(true);

        SOSInternAuthSession s = (SOSInternAuthSession) as.getSession();
        s.setAccessToken(a.getAccessToken());
        s.touch();// avoid NPE
        a.setCurrentSubject(as);

        mockRootJOCControllersPermissions(a);

        return a.getAccessToken();
    }

    private void mockRootJOCPermissions(SOSAuthCurrentAccount a) {
        JocPermissions p = a.getJocPermissions();

        p.getCalendars().setView(true);
        
        p.getDailyPlan().setView(true);
        p.getDailyPlan().setManage(true);
        
        p.getFileTransfer().setView(true);
        p.getFileTransfer().setManage(true);

        p.getInventory().setView(true);
        p.getInventory().setManage(true);
        p.getInventory().setDeploy(true);

        p.getNotification().setView(true);
        p.getNotification().setManage(true);

        p.getOthers().setView(true);
        p.getOthers().setManage(true);

        p.getReports().setView(true);
    }

    private void mockRootJOCControllersPermissions(SOSAuthCurrentAccount a) {
        for (String controllerId : controllerIds) {
            ControllerPermissions p = a.getControllerPermissions(controllerId);

            p.setView(true);
            p.getAgents().setView(true);
            p.getLocks().setView(true);
            p.getLocks().setView(true);
            p.getWorkflows().setView(true);

            p.getDeployments().setView(true);
            p.getDeployments().setDeploy(true);

            p.getNoticeBoards().setView(true);
            p.getNoticeBoards().setDelete(true);
            p.getNoticeBoards().setPost(true);

            p.getOrders().setView(true);
            p.getOrders().setCancel(true);
            p.getOrders().setConfirm(true);
            p.getOrders().setCreate(true);
            p.getOrders().setManagePositions(true);
            p.getOrders().setModify(true);
            p.getOrders().setResumeFailed(true);
            p.getOrders().setSuspendResume(true);
        }
    }

    public CompletableFuture<JOCDefaultResponse> post(String methodName, StringBuilder filter) throws Exception {
        return post(methodName, filter == null ? null : filter.toString());
    }

    public CompletableFuture<JOCDefaultResponse> post(String methodName, String filter) throws Exception {
        return post(methodName, filter == null ? null : filter.getBytes("UTF-8"));
    }

    public CompletableFuture<JOCDefaultResponse> post(String methodName, Path filter) throws Exception {
        return post(methodName, filter == null ? null : Files.readAllBytes(filter));
    }

    public CompletableFuture<JOCDefaultResponse> post(String methodName, Object filter) throws Exception {
        return post(methodName, filter == null ? null : Globals.objectMapper.writeValueAsBytes(filter));
    }

    private CompletableFuture<JOCDefaultResponse> post(String methodName, byte[] filter) throws Exception {
        CompletableFuture<JOCDefaultResponse> future = CompletableFuture.supplyAsync(() -> {
            try {
                String accessToken = mockJOCLoginAsRoot();
                Method method = clazz.getDeclaredMethod(methodName, String.class, byte[].class);
                method.setAccessible(true);
                JOCDefaultResponse r = (JOCDefaultResponse) method.invoke(instance, accessToken, filter);
                Object entity = r.getEntity();
                String answer = "";
                if (entity != null) {
                    if (entity instanceof byte[]) {
                        entity = Globals.objectMapper.readValue((byte[]) entity, Object.class);
                    }
                    answer = Globals.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity);
                }
                LOGGER.info("[RESPONSE]" + answer);
                return r;
            } catch (Throwable e) {
                throw new RuntimeException("[post][" + methodName + "]" + e, e);
            }
        });
        futures.add(future);
        return future;
    }

    private void waitForAllTasksToComplete() {
        if (!futures.isEmpty()) {
            CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            f.join();
        }
    }

    public void setHibernateConfigurationFile(Path val) {
        hibernateConfigurationFile = val;
    }

    public void setHibernateConfigurationFileFromWebservicesGlobal(String fileName) {
        hibernateConfigurationFile = Paths.get("../sos-webservices-joc-global/src/test/resources/hibernate/" + fileName);
    }

    public List<String> getControllerIds() {
        return controllerIds;
    }
}
