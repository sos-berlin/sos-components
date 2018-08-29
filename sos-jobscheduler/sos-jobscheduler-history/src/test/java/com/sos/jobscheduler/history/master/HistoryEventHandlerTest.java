package com.sos.jobscheduler.history.master;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.EventHandlerSettings;

public class HistoryEventHandlerTest {

    private XPath xPath = null;

    private NodeList getInitParams(Path file) throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file.toAbsolutePath().normalize().toString());
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fis);
            xPath = XPathFactory.newInstance().newXPath();
            String expression = "/web-app/servlet/init-param";
            return (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    private NodeList getInitParamChilds(Node initParam) throws Exception {
        String expression = "./param-name|param-value";
        return (NodeList) xPath.compile(expression).evaluate(initParam, XPathConstants.NODESET);
    }

    public EventHandlerSettings getEventHandlerSettings(String webXml) throws Exception {
        EventHandlerSettings s = new EventHandlerSettings();
        EventHandlerMasterSettings ms1 = new EventHandlerMasterSettings();
        NodeList initParams = getInitParams(Paths.get(webXml));
        for (int i = 0; i < initParams.getLength(); i++) {
            NodeList initParamChilds = getInitParamChilds(initParams.item(i));
            String name = "";
            String value = null;
            for (int j = 0; j < initParamChilds.getLength(); j++) {
                Node child = initParamChilds.item(j);
                if (j == 0) {
                    name = child.getFirstChild().getNodeValue();
                } else {
                    value = child.getFirstChild() == null ? "" : child.getFirstChild().getNodeValue();
                }
            }
            System.out.println(name + "=" + value);
            switch (name) {
            case "master_id":
                ms1.setMasterId(value);
                break;
            case "master_hostname":
                ms1.setHostname(value);
                break;
            case "master_port":
                ms1.setPort(value);
                break;
            case "master_use_login":
                ms1.useLogin(Boolean.parseBoolean(value));
                break;
            case "master_user":
                ms1.setUser(value);
                break;
            case "master_user_password":
                ms1.setPassword(value);
                break;
            case "max_transactions":
                ms1.setMaxTransactions(Integer.parseInt(value));
                break;
            case "webservice_timeout":
                ms1.setWebserviceTimeout(Integer.parseInt(value));
                break;
            case "webservice_limit":
                ms1.setWebserviceLimit(Integer.parseInt(value));
                break;
            case "webservice_delay":
                ms1.setWebserviceDelay(Integer.parseInt(value));
                break;
            case "http_client_connect_timeout":
                ms1.setHttpClientConnectTimeout(Integer.parseInt(value));
                break;
            case "http_client_connection_request_timeout":
                ms1.setHttpClientConnectionRequestTimeout(Integer.parseInt(value));
                break;
            case "http_client_socket_timeout":
                ms1.setHttpClientSocketTimeout(Integer.parseInt(value));
                break;
            case "wait_interval_on_error":
                ms1.setWaitIntervalOnError(Integer.parseInt(value));
                break;
            case "wait_interval_on_empty_event":
                ms1.setWaitIntervalOnEmptyEvent(Integer.parseInt(value));
                break;
            case "max_wait_interval_on_end":
                ms1.setMaxWaitIntervalOnEnd(Integer.parseInt(value));
                break;
            case "hibernate_configuration":
                s.setHibernateConfiguration(Paths.get(value));
                break;
            case "mail_smtp_host":
                s.setMailSmtpHost(value);
                break;
            case "mail_smtp_port":
                s.setMailSmtpPort(value);
                break;
            case "mail_smtp_user":
                s.setMailSmtpUser(value);
                break;
            case "mail_smtp_password":
                s.setMailSmtpPassword(value);
                break;
            case "mail_from":
                s.setMailFrom(value);
                break;
            case "mail_to":
                s.setMailTo(value);
                break;
            }
        }
        s.addMaster(ms1);
        return s;
    }

    public static void main(String[] args) throws Exception {
        HistoryEventHandlerTest t = new HistoryEventHandlerTest();

        EventHandlerSettings s = null;
        if (args.length == 1) {
            s = t.getEventHandlerSettings(args[0]);
        } else {
            s = new EventHandlerSettings();
            EventHandlerMasterSettings ms1 = new EventHandlerMasterSettings();

            String masterId = "jobscheduler2";
            String masterHost = "localhost";
            String masterPort = "4444";
            Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");

            ms1.setMasterId(masterId);
            ms1.setHostname(masterHost);
            ms1.setPort(masterPort);
            ms1.useLogin(true);
            ms1.setUser("test");
            ms1.setPassword("12345");

            ms1.setMaxTransactions(100);

            ms1.setWebserviceTimeout(60);
            ms1.setWebserviceLimit(1000);
            ms1.setWebserviceDelay(1);

            ms1.setHttpClientConnectTimeout(30_000);
            ms1.setHttpClientConnectionRequestTimeout(30_000);
            ms1.setHttpClientSocketTimeout(75_000);

            ms1.setWaitIntervalOnError(30_000);
            ms1.setWaitIntervalOnEmptyEvent(1_000);
            ms1.setMaxWaitIntervalOnEnd(30_000);

            s.setHibernateConfiguration(hibernateConfigFile);
            s.setMailSmtpHost("localhost");
            s.setMailSmtpPort("25");
            s.setMailFrom("jobscheduler2.0@localhost");
            s.setMailTo("to@localhost");

            s.addMaster(ms1);
        }

        HistoryEventHandler eventHandler = new HistoryEventHandler(s);
        try {
            eventHandler.start();
        } catch (Exception e) {
            throw e;
        } finally {
            // Thread.sleep(1 * 60 * 1000);
            //eventHandler.exit();
        }
    }

}
