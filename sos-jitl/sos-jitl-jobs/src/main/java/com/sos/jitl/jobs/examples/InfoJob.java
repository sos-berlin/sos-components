package com.sos.jitl.jobs.examples;

import java.net.URI;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Authenticator;
import com.sos.jitl.jobs.common.Job;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class InfoJob extends ABlockingInternalJob<InfoJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoJob.class);
    private static final String CONFIG_DIR_ENV_KEY = "JS7_AGENT_CONFIG_DIR";
    private static final String WS_API_LOGIN = "/joc/api/authentication/login";
    private static final String WS_API_LOGOUT = "/joc/api/authentication/logout";
    private static final String TEST_WEB_SERVER_URI = "https://joc-2-0-secondary.sos:7543";
    
    private SOSRestApiClient httpsRestApiClient = null;
    private String accessToken = null;

    public InfoJob(JobContext jobContext) {
        super(jobContext, InfoJobArguments.class);
        LOGGER.info("[CONSTRUCTOR]jobArguments=" + Job.convert(getJobContext().jobArguments()));
    }

    @Override
    public void onStart(InfoJobArguments args) throws Exception {
        LOGGER.info("start");
        LOGGER.info("System.getProperty(...): " + System.getProperty(CONFIG_DIR_ENV_KEY));
        LOGGER.info("System.getenv(...): " + System.getenv(CONFIG_DIR_ENV_KEY));
        String privateConfPath = Paths.get("/").resolve(System.getenv(CONFIG_DIR_ENV_KEY)).resolve("private").resolve("private.conf").normalize().toString().replace('\\', '/');
        httpsRestApiClient = Authenticator.createHttpsRestApiClient(privateConfPath);
        URI jocLoginURI = URI.create(TEST_WEB_SERVER_URI + WS_API_LOGIN);
        accessToken = Authenticator.login(httpsRestApiClient, jocLoginURI);
		LOGGER.info("Logged in!");
        LOGGER.info("accessToken: " + accessToken);
    }

    @Override
    public void onStop(InfoJobArguments args) {
        if (accessToken != null && httpsRestApiClient != null) {
        	try {
        		URI jocLogoutURI = URI.create(TEST_WEB_SERVER_URI + WS_API_LOGOUT);
				Authenticator.logout(httpsRestApiClient, accessToken, jocLogoutURI);
				LOGGER.info("Logged out!");
			} catch (SOSException e) {
				LOGGER.error(e.getMessage(), e);
			}
        } else if(httpsRestApiClient != null) {
        	httpsRestApiClient.closeHttpClient();
        }
        LOGGER.info("stop");
    }

    @Override
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, InfoJobArguments args) throws Exception {
        LOGGER.info("processOrder: " + step.order().id());

        long result = 1;
        usePrintWriter(step, result);
        // useWriter(step, result);

        return Job.success("info_result", result);
    }

    private void usePrintWriter(BlockingInternalJob.Step step, long result) throws Exception {
        Job.info(step, "[OUT]----------ENV-----------------");
        Job.info(step, "[OUT]    JS7");
        System.getenv().entrySet().stream().filter(e -> e.getKey().startsWith("JS7")).forEach(e -> {
            Job.info(step, "        " + e.getKey() + "=" + e.getValue());
        });
        Job.info(step, "[OUT]    System");
        System.getenv().entrySet().stream().filter(e -> !e.getKey().startsWith("JS7")).forEach(e -> {
            Job.info(step, "        " + e.getKey() + "=" + e.getValue());
        });

        Job.info(step, "[OUT]----------Workflow-----------------");
        Job.info(step, "[OUT]name=" + Job.getWorkflowName(step));
        Job.info(step, "[OUT]versionId=" + Job.getWorkflowVersionId(step));
        Job.info(step, "[OUT]position=" + Job.getWorkflowPosition(step));

        Job.info(step, "[OUT]----------ORDER-----------------");
        Job.info(step, "[OUT]id: " + Job.getOrderId(step));
        Job.info(step, "[OUT]arguments(scala): " + step.order().arguments());
        Job.info(step, "[OUT]arguments(java): " + Job.convert(step.order().arguments()));

        Job.info(step, "[OUT]----------JOB-----------------");
        Job.info(step, "[OUT]arguments(scala): " + getJobContext().jobArguments());
        Job.info(step, "[OUT]arguments(java): " + Job.convert(getJobContext().jobArguments()));

        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        Job.info(step, "[OUT]----------NODE-----------------");
        Job.info(step, "[OUT]agentId: " + Job.getAgentId(step));
        Job.info(step, "[OUT]name=" + Job.getJobName(step));
        Job.info(step, "[OUT]arguments(scala): " + step.arguments());
        Job.info(step, "[OUT]arguments(java): " + Job.convert(step.arguments()));

        Job.error(step, "[ERR]position written to err=" + Job.getWorkflowPosition(step));

        Job.info(step, "[OUT]----------RETURN-----------------");
        Job.info(step, "[OUT]returns Succeeded and \"info_result\"=" + result);
    }

    @SuppressWarnings("unused")
    private void useWriter(BlockingInternalJob.Step step, long result) throws Exception {
        String newLine = "\n";

        step.outWriter().write("[outWriter]----------Workflow-----------------" + newLine);
        step.outWriter().write("[outWriter]name=" + Job.getWorkflowName(step) + newLine);
        step.outWriter().write("[outWriter]versionId=" + Job.getWorkflowVersionId(step) + newLine);
        step.outWriter().write("[outWriter]position=" + Job.getWorkflowPosition(step) + newLine);

        step.outWriter().write("[outWriter]----------ORDER-----------------" + newLine);
        step.outWriter().write("[outWriter]id: " + Job.getOrderId(step) + newLine);
        step.outWriter().write("[outWriter]arguments(scala): " + step.order().arguments() + newLine);
        step.outWriter().write("[outWriter]arguments(java): " + Job.convert(step.order().arguments()) + newLine);

        step.outWriter().write("[outWriter]----------JOB-----------------" + newLine);
        step.outWriter().write("[outWriter]arguments(scala): " + getJobContext().jobArguments() + newLine);
        step.outWriter().write("[outWriter]arguments(java): " + Job.convert(getJobContext().jobArguments()) + newLine);

        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        step.outWriter().write("[outWriter]----------NODE-----------------" + newLine);
        step.outWriter().write("[outWriter]agentId: " + Job.getAgentId(step) + newLine);
        step.outWriter().write("[outWriter]name=" + Job.getJobName(step) + newLine);
        step.outWriter().write("[outWriter]arguments(scala): " + step.arguments() + newLine);
        step.outWriter().write("[outWriter]arguments(java): " + Job.convert(step.arguments()) + newLine);

        step.errWriter().write("[errWriter]position written to err=" + Job.getWorkflowPosition(step) + newLine);

        step.outWriter().write("[outWriter]----------RETURN-----------------" + newLine);
        step.outWriter().write("[outWriter]returns Succeeded and \"info_result\"=" + result + newLine);
    }
}
