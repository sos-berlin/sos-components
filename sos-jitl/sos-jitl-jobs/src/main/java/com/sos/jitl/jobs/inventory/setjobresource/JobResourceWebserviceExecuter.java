package com.sos.jitl.jobs.inventory.setjobresource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.model.controller.ControllerIds;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class JobResourceWebserviceExecuter {

    private static final String ENC_PREFIX = "enc:";
    private ApiExecutor apiExecutor;
    private OrderProcessStepLogger logger;

    public JobResourceWebserviceExecuter(OrderProcessStepLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    private ConfigurationObject getInventoryItem(RequestFilter requestFilter, String accessToken) throws Exception {
        logger.debug(".. getInventoryItem: path: " + requestFilter.getPath() + " , object type: " + requestFilter.getObjectType());

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(requestFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/inventory/read/configuration", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        logger.debug(".... request body: " + body);
        logger.debug("answer=" + answer);
        ConfigurationObject configurationObjectReturn = new ConfigurationObject();
        if (answer != null) {
            configurationObjectReturn = JobHelper.OBJECT_MAPPER.readValue(answer, ConfigurationObject.class);
        } else {
            configurationObjectReturn.setPath(requestFilter.getPath());
            configurationObjectReturn.setObjectType(requestFilter.getObjectType());
            configurationObjectReturn.setConfiguration(new JobResource());
        }
        return configurationObjectReturn;
    }

    private ConfigurationObject setInventoryItem(ConfigurationObject configurationObject, String accessToken) throws Exception {

        logger.debug(".. setInventoryItem: path: " + configurationObject.getPath() + " , object type: " + configurationObject.getObjectType());

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(configurationObject);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/inventory/store", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        logger.debug(".... request body: " + body);
        logger.debug("answer=" + answer);
        ConfigurationObject configurationObjectReturn = new ConfigurationObject();
        if (answer != null) {
            configurationObjectReturn = JobHelper.OBJECT_MAPPER.readValue(answer, ConfigurationObject.class);
        }
        return configurationObjectReturn;
    }

    private String getSelectedControllerId(String accessToken) throws Exception {

        logger.debug(".. getSelectedControllerId: path: ");

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString("{}");
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/controller/ids", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        ControllerIds controllerIds = new ControllerIds();
        if (answer != null) {
            controllerIds = JobHelper.OBJECT_MAPPER.readValue(answer, ControllerIds.class);
        }
        logger.debug("answer=" + answer);
        return controllerIds.getSelected();
    }

    private void publishDeployableItem(ConfigurationObject configurationObject, SetJobResourceJobArguments args, String accessToken)
            throws Exception {

        logger.debug(".. publishDeployableItem: path: " + configurationObject.getPath() + " , object type: " + configurationObject.getObjectType());
        DeployFilter deployFilter = new DeployFilter();
        deployFilter.setControllerIds(new ArrayList<String>());
        String controllerId = args.getControllerId();
        if (controllerId == null || controllerId.isEmpty()) {
            controllerId = getSelectedControllerId(accessToken);
        }
        deployFilter.getControllerIds().add(controllerId);

        DeployablesValidFilter deployablesValidFilter = new DeployablesValidFilter();
        List<Config> draftConfigurations = new ArrayList<Config>();
        com.sos.joc.model.publish.Config config = new Config();
        Configuration configuration = new Configuration();
        configuration.setObjectType(configurationObject.getObjectType());
        configuration.setPath(configurationObject.getPath());
        configuration.setRecursive(false);
        config.setConfiguration(configuration);
        draftConfigurations.add(config);

        config.setConfiguration(configuration);
        deployablesValidFilter.setDraftConfigurations(draftConfigurations);
        deployFilter.setStore(deployablesValidFilter);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(deployFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/inventory/deployment/deploy", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        logger.debug(".... request body: " + body);
        logger.debug("answer=" + answer);

    }

    private String getValue(String value, String argsTimeZone) {
        if (value.trim().startsWith("[") && value.trim().endsWith("]")) {
            Date now = new Date();
            String timeZone = "UTC";
            String pattern = value.substring(1, value.length() - 1);
            if (argsTimeZone != null && !argsTimeZone.isEmpty()) {
                timeZone = argsTimeZone;
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                Instant instant = now.toInstant();
                LocalDateTime ldt = instant.atZone(ZoneId.of(timeZone)).toLocalDateTime();
                value = ldt.format(formatter);
            } catch (IllegalArgumentException e) {
                return value;
            }
        }
        return value;
    }

    private String encrypt(SetJobResourceJobArguments args, String input, File outFile) throws CertificateException, NoSuchAlgorithmException,
            InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, SOSException {
        X509Certificate cert = null;
        PublicKey pubKey = null;
        String encryptedValue = "";

        String enciphermentCertificate = args.getEnciphermentCertificate();
        if (enciphermentCertificate.contains("CERTIFICATE")) {
            cert = KeyUtil.getX509Certificate(enciphermentCertificate);
        } else {
            try {
                pubKey = KeyUtil.getRSAPublicKeyFromString(enciphermentCertificate);
            } catch (Exception e) {
                try {
                    pubKey = KeyUtil.getECDSAPublicKeyFromString(enciphermentCertificate);
                } catch (Exception e1) {
                    try {
                        pubKey = KeyUtil.convertToRSAPublicKey(KeyUtil.stripFormatFromPublicKey(enciphermentCertificate).getBytes());
                    } catch (Exception e2) {
                        pubKey = KeyUtil.getECPublicKeyFromString(KeyUtil.stripFormatFromPublicKey(enciphermentCertificate).getBytes());
                    }
                }
            }
        }

        if (args.getFile() != null && !args.getFile().isEmpty()) {
            if (cert != null) {
                encryptedValue = com.sos.commons.encryption.executable.Encrypt.encryptFile(cert, Paths.get(args.getFile()), outFile.toPath());
            } else {
                encryptedValue = com.sos.commons.encryption.executable.Encrypt.encryptFile(pubKey, Paths.get(args.getFile()), outFile.toPath());
            }
        } else {
            if (input != null) {
                if (cert != null) {
                    encryptedValue = com.sos.commons.encryption.executable.Encrypt.encrypt(cert, input);
                } else {
                    encryptedValue = com.sos.commons.encryption.executable.Encrypt.encrypt(pubKey, input);
                }
            }
        }

        return encryptedValue;
    }

    private String getEncryptedValue(SetJobResourceJobArguments args) throws Exception {

        String value = "";
        File outFile = null;

        if (args.getEnciphermentCertificate() != null && !args.getEnciphermentCertificate().isEmpty()) {

            value = this.encrypt(args, value, outFile);
            value = ENC_PREFIX + value;

        }

        return value;

    }

    private String getEncryptedFileValue(SetJobResourceJobArguments args) throws Exception {

        String value = "";
        File outFile = null;

        if (args.getEnciphermentCertificate() != null && !args.getEnciphermentCertificate().isEmpty()) {
            outFile = Files.createTempFile("js7_setresource", ".tmp").toFile();
            args.setEncodedFileReturn(this.encrypt(args, value, outFile));
        }

        String extension = "";

        int i = args.getFile().lastIndexOf('.');
        if (i >= 0) {
            extension = args.getFile().substring(i + 1);
        }

        value = Files.readString(outFile.toPath());
        value = "to_file('" + value + "','*." + extension + "')";
        return value;
    }

    public void handleJobResource(RequestFilter requestFilter, SetJobResourceJobArguments args, String accessToken) throws Exception {
        ConfigurationObject configurationObject = this.getInventoryItem(requestFilter, accessToken);
        JobResource jobResource = (JobResource) configurationObject.getConfiguration();
        if (jobResource.getArguments() == null) {
            jobResource.setArguments(new Environment());
        }
        if (jobResource.getEnv() == null) {
            jobResource.setEnv(new Environment());
        }

        String value = "";
        if (args.getFile() != null && !args.getFile().isEmpty()) {
            value = getEncryptedFileValue(args);
            jobResource.getArguments().getAdditionalProperties().put("key_" + args.getKey(), "\"" + args.getEncodedFileReturn() + "\"");
            if (args.getEnvironmentVariable() != null && !args.getEnvironmentVariable().isEmpty()) {
                jobResource.getEnv().getAdditionalProperties().put(args.getEnvironmentVariable(), "$" + "key_" + args.getKey());
            }
        } else {
            value = getEncryptedValue(args);
        }
        jobResource.getArguments().getAdditionalProperties().put(args.getKey(), "\"" + value + "\"");
        if (args.getEnvironmentVariable() != null && !args.getEnvironmentVariable().isEmpty()) {
            jobResource.getEnv().getAdditionalProperties().put(args.getEnvironmentVariable(), "$" + args.getKey());
        }

        configurationObject.setConfiguration(jobResource);
        configurationObject = this.setInventoryItem(configurationObject, accessToken);
        publishDeployableItem(configurationObject, args, accessToken);

    }

}
