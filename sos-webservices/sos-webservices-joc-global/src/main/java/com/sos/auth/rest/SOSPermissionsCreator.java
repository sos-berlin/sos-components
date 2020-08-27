package com.sos.auth.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionCommands;
import com.sos.auth.rest.permission.model.SOSPermissionCommandsController;
import com.sos.auth.rest.permission.model.SOSPermissionCommandsControllers;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpitController;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpitControllers;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSSerializerUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.SecurityConfigurationMaster;

public class SOSPermissionsCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPermissionsCreator.class);
    private SOSShiroCurrentUser currentUser;
    private SOSPermissionRoles roles;
    private Ini ini;

    public SOSPermissionsCreator(SOSShiroCurrentUser currentUser) {
        super();
        this.currentUser = currentUser;
    }

    public void loginFromAccessToken(String accessToken) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null || Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(
                    accessToken) == null) {

                LOGGER.debug("loginFromAccessToken --> hand over session.");
                LOGGER.debug("loginFromAccessToken --> login with accessToken=" + accessToken);
                Globals.sosCockpitProperties = new JocCockpitProperties();
                Globals.setProperties();

                sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOC: loginFromAccessToken");
                LOGGER.debug("loginFromAccessToken --> hibernateSession created");
                SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
                try {
                    sosShiroIniShare.provideIniFile();
                    LOGGER.debug("loginFromAccessToken --> ini file provided");
                } catch (IOException e) {
                    throw new JocException(e);
                } catch (SOSHibernateException e) {
                    throw new DBInvalidDataException(e);
                }
                sosHibernateSession.close();

                IniSecurityManagerFactory factory = Globals.getShiroIniSecurityManagerFactory();
                SecurityManager securityManager = factory.getInstance();
                SecurityUtils.setSecurityManager(securityManager);
                LOGGER.debug("loginFromAccessToken --> securityManager created");

                SessionKey s = new DefaultSessionKey(accessToken);
                Session session = SecurityUtils.getSecurityManager().getSession(s);

                if (session != null) {
                    Subject subject = new Subject.Builder(securityManager).sessionId(accessToken).session(session).buildSubject();
                    LOGGER.debug("loginFromAccessToken --> subject created");
                    if (subject.isAuthenticated()) {

                        LOGGER.debug(getClass().getName() + ": loginFromAccessToken --> subject is authenticated");
                        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                        currentUser = new SOSShiroCurrentUser((String) subject.getPrincipals().getPrimaryPrincipal(), "", "");

                        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
                            Globals.jocWebserviceDataContainer.setCurrentUsersList(new SOSShiroCurrentUsersList());
                        }

                        LOGGER.debug("loginFromAccessToken --> removeTimedOutUser");
                        Globals.jocWebserviceDataContainer.getCurrentUsersList().removeTimedOutUser(currentUser.getUsername());

                        currentUser.setCurrentSubject(subject);
                        currentUser.setAccessToken(accessToken);

                        SOSPermissionJocCockpitControllers sosPermissionJocCockpitControllers = null;
                        if (subject.getSession().getAttribute("username_joc_permissions") != null) {
                            sosPermissionJocCockpitControllers = (SOSPermissionJocCockpitControllers) SOSSerializerUtil.fromString(subject
                                    .getSession().getAttribute("username_joc_permissions").toString());
                        } else {
                            LOGGER.warn("Could not read username_joc_permissions after fail over in the session object for access token "
                                    + accessToken);
                        }

                        LOGGER.debug("loginFromAccessToken --> JocCockpitPermissionControllerObjectList created");
                        currentUser.setSosPermissionJocCockpitControllers(sosPermissionJocCockpitControllers);
                        currentUser.initFolders();
                        LOGGER.debug(getClass().getName() + ": loginFromAccessToken --> folders initialized");

                        Section section = getIni().getSection("folders");
                        if (section != null) {
                            for (String role : section.keySet()) {
                                currentUser.addFolder(role, section.get(role));
                            }
                        }
                        SOSPermissionCommandsControllers sosPermissionCommandsControllers = (SOSPermissionCommandsControllers) SOSSerializerUtil
                                .fromString(subject.getSession().getAttribute("username_command_permissions").toString());
                        LOGGER.debug("loginFromAccessToken --> CommandsPermissionControllerObjectList created");

                        currentUser.setSosPermissionCommandsControllers(sosPermissionCommandsControllers);
                        Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);
                    }
                }
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void addSosPermissionJocCockpit(String controllerId, SOSPermissionRoles sosPermissionRoles, Set<String> unique,
            SOSPermissionJocCockpitControllers sosPermissionJocCockpitControllers) {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);
        SOSPermissionJocCockpitController sosPermissionJocCockpitMaster = new SOSPermissionJocCockpitController();
        SOSPermissionJocCockpit sosPermissionJocCockpit = sosPermissionsCreator.getSosPermissionJocCockpit(controllerId);
        sosPermissionJocCockpit.setSOSPermissionRoles(sosPermissionRoles);
        sosPermissionJocCockpit.setPrecedence(-1);
        sosPermissionJocCockpitMaster.setSOSPermissionJocCockpit(sosPermissionJocCockpit);
        sosPermissionJocCockpitMaster.setJS7Controller(controllerId);
        if (!unique.contains(controllerId)) {
            sosPermissionJocCockpitControllers.getSOSPermissionJocCockpitController().add(sosPermissionJocCockpitMaster);
            unique.add(controllerId);
        }
    }

    public SOSPermissionJocCockpitControllers createJocCockpitPermissionControllerObjectList(String accessToken,
            List<SecurityConfigurationMaster> listOfControllers) throws JocException {

        Set<String> unique = new HashSet<String>();
        SOSPermissionJocCockpitControllers sosPermissionJocCockpitControllers = new SOSPermissionJocCockpitControllers();
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);
        SOSPermissionRoles sosPermissionRoles = sosPermissionsCreator.getRoles(true);

        addSosPermissionJocCockpit("", sosPermissionRoles, unique, sosPermissionJocCockpitControllers);

        for (SecurityConfigurationMaster instance : listOfControllers) {
            if (!"".equals(instance.getMaster())) {
                addSosPermissionJocCockpit(instance.getMaster(), sosPermissionRoles, unique, sosPermissionJocCockpitControllers);
            }
        }

        return sosPermissionJocCockpitControllers;
    }

    public SOSPermissionCommandsControllers createCommandsPermissionControllerObjectList(String accessToken,
            List<SecurityConfigurationMaster> listOfControllers) throws JocException {
        Map<String, String> unique = new HashMap<String, String>();
        SOSPermissionCommandsControllers sosPermissionCommandsControllers = new SOSPermissionCommandsControllers();

        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);

        SOSPermissionCommands sosPermissionCommands = sosPermissionsCreator.getSosPermissionCommands("");
        SOSPermissionCommandsController sosPermissionCommandsController = new SOSPermissionCommandsController();
        sosPermissionCommandsController.setSOSPermissionCommands(sosPermissionCommands);
        sosPermissionCommandsController.setJS7Controller("");
        sosPermissionCommandsControllers.getSOSPermissionCommandsController().add(sosPermissionCommandsController);
        unique.put("", "");

        for (SecurityConfigurationMaster instance : listOfControllers) {
            if (!"".equals(instance.getMaster())) {
                sosPermissionCommandsController = new SOSPermissionCommandsController();

                sosPermissionCommands = sosPermissionsCreator.getSosPermissionCommands(instance.getMaster());
                sosPermissionCommandsController.setSOSPermissionCommands(sosPermissionCommands);
                sosPermissionCommandsController.setJS7Controller(instance.getMaster());
                if (unique.get(sosPermissionCommandsController.getJS7Controller()) == null) {
                    sosPermissionCommandsControllers.getSOSPermissionCommandsController().add(sosPermissionCommandsController);
                    unique.put(sosPermissionCommandsController.getJS7Controller(), "");
                }
            }
        }

        return sosPermissionCommandsControllers;
    }

    protected SOSPermissionJocCockpit getSosPermissionJocCockpit(String controllerId) {

        ObjectFactory o = new ObjectFactory();

        SOSPermissionJocCockpit sosPermissionJocCockpit = o.createSOSPermissionJocCockpit();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionJocCockpit.setIsAuthenticated(currentUser.isAuthenticated());
            sosPermissionJocCockpit.setAccessToken(currentUser.getAccessToken());
            sosPermissionJocCockpit.setUser(currentUser.getUsername());

            sosPermissionJocCockpit.setJS7Controller(o.createSOSPermissionJocCockpitJS7Controller());
            sosPermissionJocCockpit.setJS7ControllerCluster(o.createSOSPermissionJocCockpitJS7ControllerCluster());
            sosPermissionJocCockpit.setJS7UniversalAgent(o.createSOSPermissionJocCockpitJS7UniversalAgent());
            sosPermissionJocCockpit.setDailyPlan(o.createSOSPermissionJocCockpitDailyPlan());
            sosPermissionJocCockpit.setHistory(o.createSOSPermissionJocCockpitHistory());
            sosPermissionJocCockpit.setOrder(o.createSOSPermissionJocCockpitOrder());
            sosPermissionJocCockpit.setWorkflow(o.createSOSPermissionJocCockpitWorkflow());
            sosPermissionJocCockpit.setJob(o.createSOSPermissionJocCockpitJob());
            sosPermissionJocCockpit.setProcessClass(o.createSOSPermissionJocCockpitProcessClass());
            sosPermissionJocCockpit.setLock(o.createSOSPermissionJocCockpitLock());
            sosPermissionJocCockpit.setHolidayCalendar(o.createSOSPermissionJocCockpitHolidayCalendar());
            sosPermissionJocCockpit.setAuditLog(o.createSOSPermissionJocCockpitAuditLog());
            sosPermissionJocCockpit.setMaintenanceWindow(o.createSOSPermissionJocCockpitMaintenanceWindow());
            sosPermissionJocCockpit.setYADE(o.createSOSPermissionJocCockpitYADE());
            sosPermissionJocCockpit.setRuntime(o.createSOSPermissionJocCockpitRuntime());
            sosPermissionJocCockpit.getRuntime().setExecute(o.createSOSPermissionJocCockpitRuntimeExecute());
            sosPermissionJocCockpit.setJoc(o.createSOSPermissionJocCockpitJoc());
            sosPermissionJocCockpit.getJoc().setView(o.createSOSPermissionJocCockpitJocView());
            sosPermissionJocCockpit.setJobStream(o.createSOSPermissionJocCockpitJobStream());
            sosPermissionJocCockpit.getJobStream().setChange(o.createSOSPermissionJocCockpitJobStreamChange());
            sosPermissionJocCockpit.getJobStream().setView(o.createSOSPermissionJocCockpitJobStreamView());
            sosPermissionJocCockpit.getJobStream().getChange().setEvents(o.createSOSPermissionJocCockpitJobStreamChangeEvents());

            sosPermissionJocCockpit.setCalendar(o.createSOSPermissionJocCockpitCalendar());
            sosPermissionJocCockpit.getCalendar().setView(o.createSOSPermissionJocCockpitCalendarView());
            sosPermissionJocCockpit.getCalendar().setEdit(o.createSOSPermissionJocCockpitCalendarEdit());
            sosPermissionJocCockpit.getCalendar().getEdit().setAssign(o.createSOSPermissionJocCockpitCalendarEditAssign());

            sosPermissionJocCockpit.setJOCConfigurations(o.createSOSPermissionJocCockpitJOCConfigurations());
            sosPermissionJocCockpit.getJOCConfigurations().setShare(o.createSOSPermissionJocCockpitJOCConfigurationsShare());
            sosPermissionJocCockpit.getJOCConfigurations().getShare().setView(o.createSOSPermissionJocCockpitJOCConfigurationsShareView());
            sosPermissionJocCockpit.getJOCConfigurations().getShare().setChange(o.createSOSPermissionJocCockpitJOCConfigurationsShareChange());
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().setSharedStatus(o
                    .createSOSPermissionJocCockpitJOCConfigurationsShareChangeSharedStatus());

            sosPermissionJocCockpit.getJS7Controller().setAdministration(o.createSOSPermissionJocCockpitJS7ControllerAdministration());

            sosPermissionJocCockpit.getJS7Controller().setView(o.createSOSPermissionJocCockpitJS7ControllerView());
            sosPermissionJocCockpit.getJS7Controller().setExecute(o.createSOSPermissionJocCockpitJS7ControllerExecute());
            sosPermissionJocCockpit.getJS7Controller().getExecute().setRestart(o.createSOSPermissionJocCockpitJS7ControllerExecuteRestart());

            sosPermissionJocCockpit.setDocumentation(o.createSOSPermissionJocCockpitDocumentation());

            sosPermissionJocCockpit.getJS7ControllerCluster().setView(o.createSOSPermissionJocCockpitJS7ControllerClusterView());
            sosPermissionJocCockpit.getJS7ControllerCluster().setExecute(o.createSOSPermissionJocCockpitJS7ControllerClusterExecute());

            sosPermissionJocCockpit.getJS7UniversalAgent().setView(o.createSOSPermissionJocCockpitJS7UniversalAgentView());
            sosPermissionJocCockpit.getJS7UniversalAgent().setExecute(o.createSOSPermissionJocCockpitJS7UniversalAgentExecute());
            sosPermissionJocCockpit.getJS7UniversalAgent().getExecute().setRestart(o.createSOSPermissionJocCockpitJS7UniversalAgentExecuteRestart());

            sosPermissionJocCockpit.getDailyPlan().setView(o.createSOSPermissionJocCockpitDailyPlanView());
            sosPermissionJocCockpit.getOrder().setView(o.createSOSPermissionJocCockpitOrderView());
            sosPermissionJocCockpit.getOrder().setChange(o.createSOSPermissionJocCockpitOrderChange());
            sosPermissionJocCockpit.getOrder().setDelete(o.createSOSPermissionJocCockpitOrderDelete());
            sosPermissionJocCockpit.getOrder().setExecute(o.createSOSPermissionJocCockpitOrderExecute());

            sosPermissionJocCockpit.getWorkflow().setView(o.createSOSPermissionJocCockpitWorkflowView());
            sosPermissionJocCockpit.getWorkflow().setExecute(o.createSOSPermissionJocCockpitWorkflowExecute());

            sosPermissionJocCockpit.getJob().setView(o.createSOSPermissionJocCockpitJobView());
            sosPermissionJocCockpit.getJob().setChange(o.createSOSPermissionJocCockpitJobChange());
            sosPermissionJocCockpit.getJob().setExecute(o.createSOSPermissionJocCockpitJobExecute());

            sosPermissionJocCockpit.getProcessClass().setView(o.createSOSPermissionJocCockpitProcessClassView());

            sosPermissionJocCockpit.getLock().setView(o.createSOSPermissionJocCockpitLockView());

            sosPermissionJocCockpit.getHolidayCalendar().setView(o.createSOSPermissionJocCockpitHolidayCalendarView());
            sosPermissionJocCockpit.getAuditLog().setView(o.createSOSPermissionJocCockpitAuditLogView());
            sosPermissionJocCockpit.getMaintenanceWindow().setView(o.createSOSPermissionJocCockpitMaintenanceWindowView());
            sosPermissionJocCockpit.getHistory().setView(o.createSOSPermissionJocCockpitHistoryView());

            sosPermissionJocCockpit.getYADE().setView(o.createSOSPermissionJocCockpitYADEView());
            sosPermissionJocCockpit.getYADE().setExecute(o.createSOSPermissionJocCockpitYADEExecute());
            sosPermissionJocCockpit.getYADE().setConfigurations(o.createSOSPermissionJocCockpitYADEConfigurations());

            sosPermissionJocCockpit.setInventory(o.createSOSPermissionJocCockpitInventory());
            sosPermissionJocCockpit.getInventory().setConfigurations(o.createSOSPermissionJocCockpitInventoryConfigurations());
            sosPermissionJocCockpit.getInventory().getConfigurations().setPublish(o.createSOSPermissionJocCockpitInventoryConfigurationsPublish());

            
            
            sosPermissionJocCockpit.getJoc().getView().setLog(haveRight(controllerId, "sos:products:joc_cockpit:joc:view:log"));

            sosPermissionJocCockpit.getJS7Controller().getView().setStatus(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:view:status"));
            sosPermissionJocCockpit.getJS7Controller().getView().setMainlog(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:view:mainlog"));
            sosPermissionJocCockpit.getJS7Controller().getView().setParameter(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:view:parameter"));
            sosPermissionJocCockpit.getJS7Controller().getExecute().getRestart().setAbort(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:execute:restart:abort"));
            sosPermissionJocCockpit.getJS7Controller().getExecute().getRestart().setTerminate(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:execute:restart:terminate"));
            sosPermissionJocCockpit.getJS7Controller().getExecute().setPause(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:execute:pause"));
            sosPermissionJocCockpit.getJS7Controller().getExecute().setContinue(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:execute:continue"));
            sosPermissionJocCockpit.getJS7Controller().getExecute().setTerminate(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:execute:terminate"));
            sosPermissionJocCockpit.getJS7Controller().getExecute().setAbort(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:execute:abort"));
            sosPermissionJocCockpit.getJS7Controller().getAdministration().setManageCategories(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:administration:manage_categories"));
            sosPermissionJocCockpit.getJS7Controller().getAdministration().setEditPermissions(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:administration:edit_permissions"));
            sosPermissionJocCockpit.getJS7Controller().getAdministration().setRemoveOldInstances(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller:administration:remove_old_instances"));

            sosPermissionJocCockpit.getInventory().getConfigurations().setDelete(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:delete"));
            sosPermissionJocCockpit.getInventory().getConfigurations().setEdit(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:edit"));
            sosPermissionJocCockpit.getInventory().getConfigurations().setView(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:view"));
          
            sosPermissionJocCockpit.getInventory().getConfigurations().getPublish().setDeploy(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:publish:deploy"));
            sosPermissionJocCockpit.getInventory().getConfigurations().getPublish().setSetVersion(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:publish:set_version"));
            sosPermissionJocCockpit.getInventory().getConfigurations().getPublish().setImport(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:publish:import"));
            sosPermissionJocCockpit.getInventory().getConfigurations().getPublish().setExport(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:publish:export"));
            sosPermissionJocCockpit.getInventory().getConfigurations().getPublish().setGenerateKey(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:publish:generateKey"));
            sosPermissionJocCockpit.getInventory().getConfigurations().getPublish().setShowKey(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:publish:showKey"));
            sosPermissionJocCockpit.getInventory().getConfigurations().getPublish().setImportKey(haveRight(controllerId,
                    "sos:products:joc_cockpit:inventory:configurations:publish:importKey"));

            sosPermissionJocCockpit.getJS7ControllerCluster().getView().setStatus(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller_cluster:view:status"));
            sosPermissionJocCockpit.getJS7ControllerCluster().getExecute().setTerminateFailSafe(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller_cluster:execute:terminate_fail_safe"));
            sosPermissionJocCockpit.getJS7ControllerCluster().getExecute().setRestart(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller_cluster:execute:restart"));
            sosPermissionJocCockpit.getJS7ControllerCluster().getExecute().setTerminate(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_controller_cluster:execute:terminate"));

            sosPermissionJocCockpit.getJS7UniversalAgent().getView().setStatus(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_universal_agent:view:status"));
            sosPermissionJocCockpit.getJS7UniversalAgent().getExecute().getRestart().setAbort(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_universal_agent:execute:restart:abort"));
            sosPermissionJocCockpit.getJS7UniversalAgent().getExecute().getRestart().setTerminate(haveRight(controllerId,
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:restart:terminate"));
            sosPermissionJocCockpit.getJS7UniversalAgent().getExecute().setAbort(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_universal_agent:execute:abort"));
            sosPermissionJocCockpit.getJS7UniversalAgent().getExecute().setTerminate(haveRight(controllerId,
                    "sos:products:joc_cockpit:js7_universal_agent:execute:terminate"));

            sosPermissionJocCockpit.getDocumentation().setView(haveRight(controllerId, "sos:products:joc_cockpit:documentation:view"));
            sosPermissionJocCockpit.getDocumentation().setImport(haveRight(controllerId, "sos:products:joc_cockpit:documentation:import"));
            sosPermissionJocCockpit.getDocumentation().setExport(haveRight(controllerId, "sos:products:joc_cockpit:documentation:export"));
            sosPermissionJocCockpit.getDocumentation().setDelete(haveRight(controllerId, "sos:products:joc_cockpit:documentation:delete"));

            sosPermissionJocCockpit.getDailyPlan().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:daily_plan:view:status"));

            sosPermissionJocCockpit.getHistory().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:history:view:status"));

            sosPermissionJocCockpit.getOrder().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:order:view:status"));
            sosPermissionJocCockpit.getOrder().getView().setConfiguration(haveRight(controllerId,
                    "sos:products:joc_cockpit:order:view:configuration"));
            sosPermissionJocCockpit.getOrder().getView().setOrderLog(haveRight(controllerId, "sos:products:joc_cockpit:order:view:order_log"));
            sosPermissionJocCockpit.getOrder().getView().setDocumentation(haveRight(controllerId,
                    "sos:products:joc_cockpit:order:view:documentation"));
            sosPermissionJocCockpit.getOrder().getChange().setStartAndEndNode(haveRight(controllerId,
                    "sos:products:joc_cockpit:order:change:start_and_end_node"));
            sosPermissionJocCockpit.getOrder().getChange().setTimeForAdhocOrder(haveRight(controllerId,
                    "sos:products:joc_cockpit:order:change:time_for_adhoc_orders"));
            sosPermissionJocCockpit.getOrder().getChange().setParameter(haveRight(controllerId, "sos:products:joc_cockpit:order:change:parameter"));
            sosPermissionJocCockpit.getOrder().getChange().setRunTime(haveRight(controllerId, "sos:products:joc_cockpit:order:change:run_time"));
            sosPermissionJocCockpit.getOrder().getChange().setState(haveRight(controllerId, "sos:products:joc_cockpit:order:change:state"));
            sosPermissionJocCockpit.getOrder().getExecute().setStart(haveRight(controllerId, "sos:products:joc_cockpit:order:execute:start"));
            sosPermissionJocCockpit.getOrder().getExecute().setUpdate(haveRight(controllerId, "sos:products:joc_cockpit:order:execute:update"));
            sosPermissionJocCockpit.getOrder().getExecute().setSuspend(haveRight(controllerId, "sos:products:joc_cockpit:order:execute:suspend"));
            sosPermissionJocCockpit.getOrder().getExecute().setResume(haveRight(controllerId, "sos:products:joc_cockpit:order:execute:resume"));
            sosPermissionJocCockpit.getOrder().getExecute().setReset(haveRight(controllerId, "sos:products:joc_cockpit:order:execute:reset"));
            sosPermissionJocCockpit.getOrder().getExecute().setRemoveSetback(haveRight(controllerId,
                    "sos:products:joc_cockpit:order:execute:remove_setback"));
            sosPermissionJocCockpit.getOrder().getDelete().setPermanent(haveRight(controllerId, "sos:products:joc_cockpit:order:delete:permanent"));
            sosPermissionJocCockpit.getOrder().getDelete().setTemporary(haveRight(controllerId, "sos:products:joc_cockpit:order:delete:temporary"));
            sosPermissionJocCockpit.getOrder().setAssignDocumentation(haveRight(controllerId, "sos:products:joc_cockpit:order:assign_documentation"));

            sosPermissionJocCockpit.getWorkflow().getView().setConfiguration(haveRight(controllerId,
                    "sos:products:joc_cockpit:workflow:view:configuration"));
            sosPermissionJocCockpit.getWorkflow().getView().setHistory(haveRight(controllerId, "sos:products:joc_cockpit:workflow:view:history"));
            sosPermissionJocCockpit.getWorkflow().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:workflow:view:status"));
            sosPermissionJocCockpit.getWorkflow().getView().setDocumentation(haveRight(controllerId,
                    "sos:products:joc_cockpit:workflow:view:documentation"));
            sosPermissionJocCockpit.getWorkflow().getExecute().setStop(haveRight(controllerId, "sos:products:joc_cockpit:workflow:execute:stop"));
            sosPermissionJocCockpit.getWorkflow().getExecute().setUnstop(haveRight(controllerId, "sos:products:joc_cockpit:workflow:execute:unstop"));
            sosPermissionJocCockpit.getWorkflow().getExecute().setAddOrder(haveRight(controllerId,
                    "sos:products:joc_cockpit:workflow:execute:add_order"));
            sosPermissionJocCockpit.getWorkflow().getExecute().setSkipWorkflowNode(haveRight(controllerId,
                    "sos:products:joc_cockpit:workflow:execute:skip_workflow_node"));
            sosPermissionJocCockpit.getWorkflow().getExecute().setProcessWorkflowNode(haveRight(controllerId,
                    "sos:products:joc_cockpit:workflow:execute:process_workflow_node"));
            sosPermissionJocCockpit.getWorkflow().getExecute().setStopWorkflowNode(haveRight(controllerId,
                    "sos:products:joc_cockpit:workflow:execute:stop_workflow_node"));
            sosPermissionJocCockpit.getWorkflow().setAssignDocumentation(haveRight(controllerId,
                    "sos:products:joc_cockpit:workflow:assign_documentation"));

            sosPermissionJocCockpit.getJob().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:job:view:status"));
            sosPermissionJocCockpit.getJob().getView().setTaskLog(haveRight(controllerId, "sos:products:joc_cockpit:job:view:task_log"));
            sosPermissionJocCockpit.getJob().getView().setConfiguration(haveRight(controllerId, "sos:products:joc_cockpit:job:view:configuration"));
            sosPermissionJocCockpit.getJob().getView().setHistory(haveRight(controllerId, "sos:products:joc_cockpit:job:view:history"));
            sosPermissionJocCockpit.getJob().getView().setDocumentation(haveRight(controllerId, "sos:products:joc_cockpit:job:view:documentation"));
            sosPermissionJocCockpit.getJob().getChange().setRunTime(haveRight(controllerId, "sos:products:joc_cockpit:job:change:run_time"));
            sosPermissionJocCockpit.getJob().getExecute().setStart(haveRight(controllerId, "sos:products:joc_cockpit:job:execute:start"));
            sosPermissionJocCockpit.getJob().getExecute().setStop(haveRight(controllerId, "sos:products:joc_cockpit:job:execute:stop"));
            sosPermissionJocCockpit.getJob().getExecute().setUnstop(haveRight(controllerId, "sos:products:joc_cockpit:job:execute:unstop"));
            sosPermissionJocCockpit.getJob().getExecute().setTerminate(haveRight(controllerId, "sos:products:joc_cockpit:job:execute:terminate"));
            sosPermissionJocCockpit.getJob().getExecute().setKill(haveRight(controllerId, "sos:products:joc_cockpit:job:execute:kill"));
            sosPermissionJocCockpit.getJob().getExecute().setEndAllTasks(haveRight(controllerId,
                    "sos:products:joc_cockpit:job:execute:end_all_tasks"));
            sosPermissionJocCockpit.getJob().getExecute().setSuspendAllTasks(haveRight(controllerId,
                    "sos:products:joc_cockpit:job:execute:suspend_all_tasks"));
            sosPermissionJocCockpit.getJob().getExecute().setContinueAllTasks(haveRight(controllerId,
                    "sos:products:joc_cockpit:job:execute:continue_all_tasks"));
            sosPermissionJocCockpit.getJob().setAssignDocumentation(haveRight(controllerId, "sos:products:joc_cockpit:job:assign_documentation"));

            sosPermissionJocCockpit.getProcessClass().getView().setStatus(haveRight(controllerId,
                    "sos:products:joc_cockpit:process_class:view:status"));
            sosPermissionJocCockpit.getProcessClass().getView().setConfiguration(haveRight(controllerId,
                    "sos:products:joc_cockpit:process_class:view:configuration"));
            sosPermissionJocCockpit.getProcessClass().getView().setDocumentation(haveRight(controllerId,
                    "sos:products:joc_cockpit:process_class:view:documentation"));
            sosPermissionJocCockpit.getProcessClass().setAssignDocumentation(haveRight(controllerId,
                    "sos:products:joc_cockpit:process_class:assign_documentation"));

            sosPermissionJocCockpit.getLock().getView().setConfiguration(haveRight(controllerId, "sos:products:joc_cockpit:lock:view:configuration"));
            sosPermissionJocCockpit.getLock().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:lock:view:status"));
            sosPermissionJocCockpit.getLock().getView().setDocumentation(haveRight(controllerId, "sos:products:joc_cockpit:lock:view:documentation"));
            sosPermissionJocCockpit.getLock().setAssignDocumentation(haveRight(controllerId, "sos:products:joc_cockpit:lock:assign_documentation"));

            sosPermissionJocCockpit.getHolidayCalendar().getView().setStatus(haveRight(controllerId,
                    "sos:products:joc_cockpit:holiday_calendar:view:status"));
            sosPermissionJocCockpit.getAuditLog().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:audit_log:view:status"));

            sosPermissionJocCockpit.getJOCConfigurations().getShare().getView().setStatus(haveRight(controllerId,
                    "sos:products:joc_cockpit:customization:share:view:status"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().setDelete(haveRight(controllerId,
                    "sos:products:joc_cockpit:customization:share:change:delete"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().setEditContent(haveRight(controllerId,
                    "sos:products:joc_cockpit:customization:share:change:edit_content"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().getSharedStatus().setMakePrivate(haveRight(controllerId,
                    "sos:products:joc_cockpit:customization:share:change:shared_status:make_private"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().getSharedStatus().setMakeShared(haveRight(controllerId,
                    "sos:products:joc_cockpit:customization:share:change:shared_status:make_share"));

            sosPermissionJocCockpit.getMaintenanceWindow().getView().setStatus(haveRight(controllerId,
                    "sos:products:joc_cockpit:maintenance_window:view:status"));
            sosPermissionJocCockpit.getMaintenanceWindow().setEnableDisableMaintenanceWindow(haveRight(controllerId,
                    "sos:products:joc_cockpit:maintenance_window:enable_disable_maintenance_window"));

            sosPermissionJocCockpit.getYADE().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:yade:view:status"));
            sosPermissionJocCockpit.getYADE().getView().setFiles(haveRight(controllerId, "sos:products:joc_cockpit:yade:view:files"));
            sosPermissionJocCockpit.getYADE().getExecute().setTransferStart(haveRight(controllerId,
                    "sos:products:joc_cockpit:yade:execute:transfer_start"));
            sosPermissionJocCockpit.getYADE().getConfigurations().setDelete(haveRight(controllerId,
                    "sos:products:joc_cockpit:yade:configurations:delete"));
            sosPermissionJocCockpit.getYADE().getConfigurations().setDeploy(haveRight(controllerId,
                    "sos:products:joc_cockpit:yade:configurations:deploy"));
            sosPermissionJocCockpit.getYADE().getConfigurations().setEdit(haveRight(controllerId,
                    "sos:products:joc_cockpit:yade:configurations:edit"));
            sosPermissionJocCockpit.getYADE().getConfigurations().setView(haveRight(controllerId,
                    "sos:products:joc_cockpit:yade:configurations:view"));

            sosPermissionJocCockpit.getCalendar().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:calendar:view:status"));
            sosPermissionJocCockpit.getCalendar().getView().setDocumentation(haveRight(controllerId,
                    "sos:products:joc_cockpit:calendar:view:documentation"));
            sosPermissionJocCockpit.getCalendar().getEdit().setChange(haveRight(controllerId, "sos:products:joc_cockpit:calendar:edit:change"));
            sosPermissionJocCockpit.getCalendar().getEdit().setDelete(haveRight(controllerId, "sos:products:joc_cockpit:calendar:edit:delete"));
            sosPermissionJocCockpit.getCalendar().getEdit().setCreate(haveRight(controllerId, "sos:products:joc_cockpit:calendar:edit:create"));
            sosPermissionJocCockpit.getCalendar().setAssignDocumentation(haveRight(controllerId,
                    "sos:products:joc_cockpit:calendar:assign_documentation"));

            sosPermissionJocCockpit.getCalendar().getEdit().getAssign().setChange(haveRight(controllerId,
                    "sos:products:joc_cockpit:calendar:assign:change"));
            sosPermissionJocCockpit.getCalendar().getEdit().getAssign().setNonworking(haveRight(controllerId,
                    "sos:products:joc_cockpit:calendar:assign:nonworking"));
            sosPermissionJocCockpit.getCalendar().getEdit().getAssign().setRuntime(haveRight(controllerId,
                    "sos:products:joc_cockpit:calendar:assign:runtime"));

            sosPermissionJocCockpit.getRuntime().getExecute().setEditXml(haveRight(controllerId,
                    "sos:products:joc_cockpit:runtime:execute:edit_xml"));

            sosPermissionJocCockpit.setJobStream(o.createSOSPermissionJocCockpitJobStream());
            sosPermissionJocCockpit.getJobStream().setView(o.createSOSPermissionJocCockpitJobStreamView());
            sosPermissionJocCockpit.getJobStream().setChange(o.createSOSPermissionJocCockpitJobStreamChange());
            sosPermissionJocCockpit.getJobStream().getChange().setEvents(o.createSOSPermissionJocCockpitJobStreamChangeEvents());

            sosPermissionJocCockpit.getJobStream().getView().setEventlist(haveRight(controllerId,
                    "sos:products:joc_cockpit:jobstream:view:eventlist"));
            sosPermissionJocCockpit.getJobStream().getView().setGraph(haveRight(controllerId, "sos:products:joc_cockpit:jobstream:view:graph"));
            sosPermissionJocCockpit.getJobStream().getView().setStatus(haveRight(controllerId, "sos:products:joc_cockpit:jobstream:view:status"));
            sosPermissionJocCockpit.getJobStream().getChange().setConditions(haveRight(controllerId,
                    "sos:products:joc_cockpit:jobstream:change:conditions"));
            sosPermissionJocCockpit.getJobStream().getChange().getEvents().setAdd(haveRight(controllerId,
                    "sos:products:joc_cockpit:jobstream:change:events:add"));
            sosPermissionJocCockpit.getJobStream().getChange().getEvents().setRemove(haveRight(controllerId,
                    "sos:products:joc_cockpit:jobstream:change:events:remove"));
            sosPermissionJocCockpit.getJobStream().getChange().setJobStream(haveRight(controllerId,
                    "sos:products:joc_cockpit:jobstream:change:jobStream"));

        }
        return sosPermissionJocCockpit;
    }

    protected SOSPermissionCommands getSosPermissionCommands(String controllerId) {

        ObjectFactory o = new ObjectFactory();

        SOSPermissionCommands sosPermissionCommands = o.createSOSPermissionCommands();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionCommands.setIsAuthenticated(currentUser.isAuthenticated());
            sosPermissionCommands.setAccessToken(currentUser.getAccessToken());
            sosPermissionCommands.setUser(currentUser.getUsername());

            sosPermissionCommands.setDailyPlan(o.createSOSPermissionCommandsDailyPlan());
            sosPermissionCommands.getDailyPlan().setView(o.createSOSPermissionCommandsDailyPlanView());
            sosPermissionCommands.setJS7Controller(o.createSOSPermissionCommandsJS7Controller());
            sosPermissionCommands.setJS7ControllerCluster(o.createSOSPermissionCommandsJS7ControllerCluster());
            sosPermissionCommands.setHistory(o.createSOSPermissionCommandsHistory());
            sosPermissionCommands.setOrder(o.createSOSPermissionCommandsOrder());
            sosPermissionCommands.setWorkflow(o.createSOSPermissionCommandsWorkflow());
            sosPermissionCommands.setJob(o.createSOSPermissionCommandsJob());
            sosPermissionCommands.setProcessClass(o.createSOSPermissionCommandsProcessClass());
            sosPermissionCommands.setLock(o.createSOSPermissionCommandsLock());

            sosPermissionCommands.getJS7Controller().setView(o.createSOSPermissionCommandsJS7ControllerView());
            sosPermissionCommands.getJS7Controller().setExecute(o.createSOSPermissionCommandsJS7ControllerExecute());
            sosPermissionCommands.getJS7Controller().setAdministration(o.createSOSPermissionCommandsJS7ControllerAdministration());
            sosPermissionCommands.getJS7Controller().getExecute().setRestart(o.createSOSPermissionCommandsJS7ControllerExecuteRestart());
            sosPermissionCommands.getJS7ControllerCluster().setExecute(o.createSOSPermissionCommandsJS7ControllerClusterExecute());

            sosPermissionCommands.getOrder().setView(o.createSOSPermissionCommandsOrderView());
            sosPermissionCommands.getOrder().setChange(o.createSOSPermissionCommandsOrderChange());
            sosPermissionCommands.getOrder().setExecute(o.createSOSPermissionCommandsOrderExecute());

            sosPermissionCommands.getWorkflow().setView(o.createSOSPermissionCommandsWorkflowView());
            sosPermissionCommands.getWorkflow().setExecute(o.createSOSPermissionCommandsWorkflowExecute());
            sosPermissionCommands.getWorkflow().setChange(o.createSOSPermissionCommandsWorkflowChange());

            sosPermissionCommands.getJob().setView(o.createSOSPermissionCommandsJobView());
            sosPermissionCommands.getJob().setExecute(o.createSOSPermissionCommandsJobExecute());
            sosPermissionCommands.getJob().setChange(o.createSOSPermissionCommandsJobChange());

            sosPermissionCommands.getProcessClass().setView(o.createSOSPermissionCommandsProcessClassView());
            sosPermissionCommands.getProcessClass().setChange(o.createSOSPermissionCommandsProcessClassChange());

            sosPermissionCommands.getLock().setView(o.createSOSPermissionCommandsLockView());
            sosPermissionCommands.getLock().setChange(o.createSOSPermissionCommandsLockChange());

            sosPermissionCommands.getJS7Controller().getView().setStatus(haveRight(controllerId, "sos:products:commands:js7_controller:view:status"));
            sosPermissionCommands.getJS7Controller().getView().setParameter(haveRight(controllerId,
                    "sos:products:commands:js7_controller:view:parameter"));
            sosPermissionCommands.getJS7Controller().getExecute().getRestart().setAbort(haveRight(controllerId,
                    "sos:products:commands:js7_controller:execute:restart:terminate"));
            sosPermissionCommands.getJS7Controller().getExecute().getRestart().setTerminate(haveRight(controllerId,
                    "sos:products:commands:js7_controller:execute:restart:abort"));
            sosPermissionCommands.getJS7Controller().getExecute().setPause(haveRight(controllerId,
                    "sos:products:commands:js7_controller:execute:pause"));
            sosPermissionCommands.getJS7Controller().getExecute().setContinue(haveRight(controllerId,
                    "sos:products:commands:js7_controller:execute:continue"));
            sosPermissionCommands.getJS7Controller().getExecute().setTerminate(haveRight(controllerId,
                    "sos:products:commands:js7_controller:execute:terminate"));
            sosPermissionCommands.getJS7Controller().getExecute().setAbort(haveRight(controllerId,
                    "sos:products:commands:js7_controller:execute:abort"));
            sosPermissionCommands.getJS7Controller().getExecute().setStop(haveRight(controllerId,
                    "sos:products:commands:js7_controller:execute:stop"));
            sosPermissionCommands.getJS7Controller().getAdministration().setManageCategories(haveRight(controllerId,
                    "sos:products:commands:js7_controller:manage_categories"));

            sosPermissionCommands.getJS7ControllerCluster().getExecute().setTerminateFailSafe(haveRight(controllerId,
                    "sos:products:commands:js7_controller_cluster:execute:terminate_fail_safe"));
            sosPermissionCommands.getJS7ControllerCluster().getExecute().setRestart(haveRight(controllerId,
                    "sos:products:commands:js7_controller_cluster:execute:restart"));
            sosPermissionCommands.getJS7ControllerCluster().getExecute().setTerminate(haveRight(controllerId,
                    "sos:products:commands:js7_controller_cluster:execute:terminate"));

            sosPermissionCommands.getDailyPlan().getView().setStatus(haveRight(controllerId, "sos:products:commands:js7_controller:view:calendar"));

            sosPermissionCommands.getHistory().setView(haveRight(controllerId, "sos:products:commands:history:view"));

            sosPermissionCommands.getOrder().getView().setStatus(haveRight(controllerId, "sos:products:commands:order:view:status"));
            sosPermissionCommands.getOrder().getChange().setStartAndEndNode(haveRight(controllerId,
                    "sos:products:commands:order:change:start_and_end_node"));
            sosPermissionCommands.getOrder().getChange().setTimeForAdhocOrder(haveRight(controllerId,
                    "sos:products:commands:order:change:time_for_adhoc_orders"));
            sosPermissionCommands.getOrder().getChange().setParameter(haveRight(controllerId, "sos:products:commands:order:change:parameter"));
            sosPermissionCommands.getOrder().getChange().setOther(haveRight(controllerId, "sos:products:commands:order:change:other"));
            sosPermissionCommands.getOrder().getChange().setRunTime(haveRight(controllerId, "sos:products:commands:order:change:run_time"));
            sosPermissionCommands.getOrder().getChange().setState(haveRight(controllerId, "sos:products:commands:order:change:state"));
            sosPermissionCommands.getOrder().getExecute().setStart(haveRight(controllerId, "sos:products:commands:order:execute:start"));
            sosPermissionCommands.getOrder().getExecute().setUpdate(haveRight(controllerId, "sos:products:commands:order:execute:update"));
            sosPermissionCommands.getOrder().getExecute().setSuspend(haveRight(controllerId, "sos:products:commands:order:execute:suspend"));
            sosPermissionCommands.getOrder().getExecute().setResume(haveRight(controllerId, "sos:products:commands:order:execute:resume"));
            sosPermissionCommands.getOrder().getExecute().setReset(haveRight(controllerId, "sos:products:commands:order:execute:reset"));
            sosPermissionCommands.getOrder().getExecute().setRemoveSetback(haveRight(controllerId,
                    "sos:products:commands:order:execute:remove_setback"));
            sosPermissionCommands.getOrder().setDelete(haveRight(controllerId, "sos:products:commands:order:delete"));

            sosPermissionCommands.getWorkflow().getView().setStatus(haveRight(controllerId, "sos:products:commands:workflow:view:status"));
            sosPermissionCommands.getWorkflow().getExecute().setStop(haveRight(controllerId, "sos:products:commands:workflow:execute:stop"));
            sosPermissionCommands.getWorkflow().getExecute().setUnstop(haveRight(controllerId, "sos:products:commands:workflow:execute:unstop"));
            sosPermissionCommands.getWorkflow().getExecute().setAddOrder(haveRight(controllerId, "sos:products:commands:workflow:execute:add_order"));
            sosPermissionCommands.getWorkflow().getExecute().setSkipJobChainNode(haveRight(controllerId,
                    "sos:products:commands:workflow:execute:skip_workflow_node"));
            sosPermissionCommands.getWorkflow().getExecute().setProcessJobChainNode(haveRight(controllerId,
                    "sos:products:commands:workflow:execute:process_workflow_node"));
            sosPermissionCommands.getWorkflow().getExecute().setStopJobChainNode(haveRight(controllerId,
                    "sos:products:commands:workflow:execute:stop_workflow_node"));
            sosPermissionCommands.getWorkflow().getExecute().setRemove(haveRight(controllerId, "sos:products:commands:workflow:remove"));

            sosPermissionCommands.getJob().getView().setStatus(haveRight(controllerId, "sos:products:commands:job:view:status"));
            sosPermissionCommands.getJob().getChange().setRunTime(haveRight(controllerId, "sos:products:commands:job:change:run_time"));
            sosPermissionCommands.getJob().getExecute().setStart(haveRight(controllerId, "sos:products:commands:job:execute:start"));
            sosPermissionCommands.getJob().getExecute().setStop(haveRight(controllerId, "sos:products:commands:job:execute:stop"));
            sosPermissionCommands.getJob().getExecute().setUnstop(haveRight(controllerId, "sos:products:commands:job:execute:unstop"));
            sosPermissionCommands.getJob().getExecute().setTerminate(haveRight(controllerId, "sos:products:commands:job:execute:terminate"));
            sosPermissionCommands.getJob().getExecute().setKill(haveRight(controllerId, "sos:products:commands:job:execute:kill"));
            sosPermissionCommands.getJob().getExecute().setEndAllTasks(haveRight(controllerId, "sos:products:commands:job:execute:end_all_tasks"));
            sosPermissionCommands.getJob().getExecute().setSuspendAllTasks(haveRight(controllerId,
                    "sos:products:commands:job:execute:suspend_all_tasks"));
            sosPermissionCommands.getJob().getExecute().setContinueAllTasks(haveRight(controllerId,
                    "sos:products:commands:job:execute:continue_all_tasks"));

            sosPermissionCommands.getProcessClass().getView().setStatus(haveRight(controllerId, "sos:products:commands:process_class:view:status"));
            sosPermissionCommands.getProcessClass().setRemove(haveRight(controllerId, "sos:products:commands:process_class:remove"));
            sosPermissionCommands.getProcessClass().getChange().setEditContent(haveRight(controllerId,
                    "sos:products:commands:process_class:change:edit_content"));
 

            sosPermissionCommands.getLock().getView().setStatus(haveRight(controllerId, "sos:products:commands:lock:view:status"));
            sosPermissionCommands.getLock().setRemove(haveRight(controllerId, "sos:products:commands:lock:remove"));
 
        }
        return sosPermissionCommands;
    }

    private boolean isPermitted(String controllerId, String permission) {
        return (currentUser != null && currentUser.isPermitted(controllerId, permission) && currentUser.isAuthenticated());
    }

    private boolean haveRight(String controllerId, String permission) {
        return isPermitted(controllerId, permission);
    }

    private void addRole(List<String> sosRoles, String role, boolean forUser) {
        if (currentUser != null && (!forUser || currentUser.hasRole(role)) && currentUser.isAuthenticated()) {
            if (!sosRoles.contains(role)) {
                sosRoles.add(role);
            }
        }
    }

    public SOSPermissionRoles getRoles(boolean forUser) {

        if (roles == null || !forUser) {
            ObjectFactory o = new ObjectFactory();
            roles = o.createSOSPermissionRoles();

            ini = getIni();
            Section s = ini.getSection("roles");

            if (s != null) {
                for (String role : s.keySet()) {
                    addRole(roles.getSOSPermissionRole(), role, forUser);
                }
            }

            s = ini.getSection("folders");
            if (s != null) {
                for (String role : s.keySet()) {
                    String[] key = role.split("\\|");
                    if (key.length == 1) {
                        addRole(roles.getSOSPermissionRole(), role, forUser);
                    }
                    if (key.length == 2) {
                        addRole(roles.getSOSPermissionRole(), key[1], forUser);
                    }
                }
            }
        }
        return roles;
    }

    public Ini getIni() {

        if (ini == null) {
            return Globals.getIniFromSecurityManagerFactory();
        }
        return ini;
    }

}