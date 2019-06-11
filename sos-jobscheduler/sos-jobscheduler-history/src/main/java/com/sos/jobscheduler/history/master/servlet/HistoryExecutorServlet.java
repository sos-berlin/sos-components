package com.sos.jobscheduler.history.master.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryExecutorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryExecutorServlet.class);

    public HistoryExecutorServlet() {
        super();
    }

    public void init() throws ServletException {

        LOGGER.info("init");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        sendOk(response);
        
        PrintWriter writer = response.getWriter();
        AsyncContext asyncContext = request.startAsync();

        asyncContext.start(new Runnable() {

            @Override
            public void run() {
                String msg = task(request);
                writer.println(msg);
                asyncContext.complete();
            }
        });
    }

    private String task(HttpServletRequest request) {
        String identifier = Thread.currentThread().getName();

        Map<String, String[]> params = request.getParameterMap();
        LOGGER.info(String.format("[%s][doPost][REQUEST]%s", identifier, params));

        try {
            int seconds = 5;
            LOGGER.info(String.format("[%s][doPost] sleep %s seconds..", identifier, seconds));
            Thread.sleep(seconds * 1_000);
            LOGGER.info(String.format("[%s][doPost] sleep end", identifier));
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[%s][doPost]%s", identifier, e.toString()), e);
        }
        return "OK";
    }

    public void doPostX(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        sendOk(response);

        String identifier = Thread.currentThread().getName();

        Map<String, String[]> params = request.getParameterMap();
        LOGGER.info(String.format("[%s][doPost][REQUEST]%s", identifier, params));

        try {
            int seconds = 5;
            LOGGER.info(String.format("[%s][doPost] sleep %s seconds..", identifier, seconds));
            Thread.sleep(seconds * 1_000);
            LOGGER.info(String.format("[%s][doPost] sleep end", identifier));
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[%s][doPost]%s", identifier, e.toString()), e);
        }
    }

    private void sendOk(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.append("sendOK");
            writer.flush();
        } catch (Exception e) {
            LOGGER.error(String.format("[sendOK]%s", e.toString()), e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("doGet");
    }

    public void destroy() {
        LOGGER.info("destroy");
    }

}
