package com.sos.jobscheduler.history.master.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.EventHandlerSettings;
import com.sos.jobscheduler.history.master.HistoryEventHandler;

public class HistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServlet.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private HistoryEventHandler eventHandler;

    public HistoryServlet() {
        super();
    }

    public void init() throws ServletException {
        String method = "init";

        logSystemInfos();
        logJVMInfos();

        try {
            eventHandler = new HistoryEventHandler(getSettings());
            LOGGER.info(String.format("[%s]timezone=%s", method, eventHandler.getTimezone()));
        } catch (Exception ex) {
            LOGGER.error(String.format("[%s]%s", method, ex.toString()), ex);
            throw new ServletException(String.format("[%s]%s", method, ex.toString()), ex);
        }
        try {
            eventHandler.start();
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", e.toString()), e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("REQUEST....");
    }

    public void destroy() {
        LOGGER.info("destroy");
        eventHandler.exit();
    }

    private void logSystemInfos() {
        try {
            String osName = System.getProperty("os.name");
            LOGGER.info(String.format("[SYSTEM]name=%s, version=%s, arch=%s", osName, System.getProperty("os.version"), System.getProperty(
                    "os.arch")));
            if (osName.startsWith("Windows")) {
                LOGGER.info(String.format("[SYSTEM]%s", System.getenv("PROCESSOR_IDENTIFIER")));
            }

        } catch (Throwable e) {
            LOGGER.error(String.format("[logSystemInfos]%s", e.toString()), e);
        }
    }

    private String getJVMMemory(long memory) {
        String msg = "no limit";
        if (memory != Long.MAX_VALUE) {
            DecimalFormat df = new DecimalFormat("0.00");
            float sizeKb = 1024.0f;
            float sizeMb = sizeKb * sizeKb;
            msg = df.format(memory / sizeMb) + " Mb";
        }
        return msg;
    }

    private void logJVMInfos() {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            // ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            // int peakThreadCount = bean.getPeakThreadCount();

            String name = runtimeBean.getName();
            String pid = name.split("@")[0];

            LOGGER.info(String.format("[JVM]pid=%s, name=%s, %s %s %s, available processors(cores)=%s, max memory=%s, input arguments=%s", pid, name,
                    System.getProperty("java.version"), runtimeBean.getVmVendor(), runtimeBean.getVmName(), Runtime.getRuntime()
                            .availableProcessors(), getJVMMemory(Runtime.getRuntime().maxMemory()), runtimeBean.getInputArguments()));

            if (isDebugEnabled) {
                String[] arr = runtimeBean.getClassPath().split(System.getProperty("path.separator"));
                for (String cp : arr) {
                    LOGGER.debug(String.format("[Classpath]%s", cp));
                }
            }

        } catch (Throwable e) {
            LOGGER.error(String.format("[logJVMInfos]%s", e.toString()), e);
        }
    }

    // TODO read from ConfigurationService
    private Properties readConfiguration(String baseDir) throws Exception {
        String method = "readConfiguration";

        String param = getInitParameter("history_configuration");
        Path path = param.contains("..") ? Paths.get(baseDir, param) : Paths.get(param);
        File file = path.toFile();
        LOGGER.info(String.format("[%s][history_configuration][%s]%s", method, path.toFile(), file.getCanonicalPath()));

        Properties conf = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the history configuration: %s", method, file.getCanonicalPath(), ex.toString()),
                    ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));
        return conf;
    }

    private Path getHibernateConfigurationFile(String baseDir, Properties conf) throws Exception {
        String param = conf.getProperty("hibernate_configuration").trim();
        return param.contains("..") ? Paths.get(baseDir, param) : Paths.get(param);
    }

    private EventHandlerSettings getSettings() throws Exception {
        String method = "getSettings";

        String jettyBase = System.getProperty("jetty.base");
        LOGGER.info(String.format("[%s][jetty_base]%s", method, jettyBase));

        Properties conf = readConfiguration(jettyBase);

        EventHandlerSettings s = null;
        try {
            s = new EventHandlerSettings();
            s.setHibernateConfiguration(getHibernateConfigurationFile(jettyBase, conf));
            s.setMailSmtpHost(conf.getProperty("mail_smtp_host").trim());
            s.setMailSmtpPort(conf.getProperty("mail_smtp_port").trim());
            s.setMailSmtpUser(conf.getProperty("mail_smtp_user").trim());
            s.setMailSmtpPassword(conf.getProperty("mail_smtp_password").trim());
            s.setMailFrom(conf.getProperty("mail_from").trim());
            s.setMailTo(conf.getProperty("mail_to").trim());

            EventHandlerMasterSettings ms = new EventHandlerMasterSettings(conf);

            LOGGER.info(String.format("[%s]%s", method, SOSString.toString(s)));
            LOGGER.info(String.format("[%s]%s", method, SOSString.toString(ms)));

            s.addMaster(ms);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            throw new ServletException(String.format("[%s]%s", method, e.toString()), e);
        }

        return s;
    }

}
