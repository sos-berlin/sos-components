package com.sos.joc.jobscheduler.impl;

import java.net.URI;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerLogResource;
import com.sos.joc.model.jobscheduler.UrlParameter;

@Path("jobscheduler")
public class JobSchedulerLogImpl extends JOCResourceImpl implements IJobSchedulerLogResource {

    private static final String LOG_API_CALL = "./jobscheduler/log";

    public JOCDefaultResponse getLog(String accessToken, UrlParameter urlParamSchema) {
        try {
            JOCDefaultResponse jocDefaultResponse = init(LOG_API_CALL, urlParamSchema, accessToken, urlParamSchema.getJobschedulerId(),
                    getPermissonsJocCockpit(urlParamSchema.getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isMainlog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            checkRequiredParameter("jobschedulerId", urlParamSchema.getJobschedulerId());
            try {
                checkRequiredParameter("url", urlParamSchema.getUrl());
            } catch (JocMissingRequiredParameterException e) {
                if (dbItemInventoryInstance.getIsCluster()) {
                    throw e;
                } else {
                    urlParamSchema.setUrl(URI.create(dbItemInventoryInstance.getUri()));
                }
            }

            // increase timeout for large log files
            int socketTimeout = Math.max(Globals.httpSocketTimeout, 30000);
            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParamSchema.getUrl(), getAccessToken());
            jocJsonCommand.setAutoCloseHttpClient(false);
            jocJsonCommand.setSocketTimeout(socketTimeout);
            jocJsonCommand.setUriBuilderForMainLog(true);
            
//            final java.nio.file.Path responseEntity = getLogPath(urlParamSchema, true);
//
//            StreamingOutput fileStream = new StreamingOutput() {
//
//                @Override
//                public void write(OutputStream output) throws IOException {
//                    InputStream in = null;
//                    try {
//                        in = Files.newInputStream(responseEntity);
//                        byte[] buffer = new byte[4096];
//                        int length;
//                        while ((length = in.read(buffer)) > 0) {
//                            output.write(buffer, 0, length);
//                        }
//                        output.flush();
//                    } finally {
//                        try {
//                            output.close();
//                        } catch (Exception e) {
//                        }
//                        if (in != null) {
//                            try {
//                                in.close();
//                            } catch (Exception e) {
//                            }
//                        }
//                        try {
//                            Files.delete(responseEntity);
//                        } catch (Exception e) {
//                        }
//                    }
//                }
//            };
//
//            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(fileStream, "master.log.gz", getSize(responseEntity));
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(jocJsonCommand.getStreamingOutputFromGet("text/plain,application/octet-stream", true), "master.log.gz");
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

//    @Override
//    public JOCDefaultResponse getDebugLogInfo(String accessToken, UrlParameter urlParamSchema) {
//        try {
//            JOCDefaultResponse jocDefaultResponse = init(LOG_API_CALL + "/info", urlParamSchema, accessToken, urlParamSchema.getJobschedulerId(),
//                    getPermissonsJocCockpit(urlParamSchema.getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isMainlog());
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//            LogInfo200 entity = new LogInfo200();
//            entity.setSurveyDate(Date.from(Instant.now()));
//
//            final java.nio.file.Path responseEntity = getLogPath(urlParamSchema, false);
//
//            entity.setDeliveryDate(Date.from(Instant.now()));
//            LogInfo logInfo = new LogInfo();
//            logInfo.setDownload(Boolean.TRUE);
//            logInfo.setFilename(responseEntity.getFileName().toString());
//            logInfo.setSize(0L);
//            try {
//                if (Files.exists(responseEntity)) {
//                    logInfo.setSize(getSize(responseEntity));
//                }
//            } catch (Exception e) {
//            }
//            entity.setLog(logInfo);
//
//            DeleteTempFile runnable = new DeleteTempFile(responseEntity);
//            new Thread(runnable).start();
//
//            return JOCDefaultResponse.responseStatus200(entity);
//        } catch (JocException e) {
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatusJSError(e);
//        } catch (Exception e) {
//            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        }
//    }

    @Override
    public JOCDefaultResponse getDebugLog(String accessToken, String queryAccessToken, String jobschedulerId, String url) {

        UrlParameter urlParams = new UrlParameter();
        urlParams.setJobschedulerId(jobschedulerId);
        urlParams.setUrl(URI.create(url));
        //urlParams.setFilename(filename);

        if (accessToken == null) {
            accessToken = queryAccessToken;
        }

        return getLog(accessToken, urlParams);
    }

    @Override
    public JOCDefaultResponse getDebugLog(String xAccessToken, UrlParameter urlParamSchema) {
        return getLog(xAccessToken, urlParamSchema);
    }

//    private java.nio.file.Path getLogPath(UrlParameter urlParamSchema, boolean withFilenameCheck)
//            throws Exception {
//
//        if (withFilenameCheck) {
//            if (urlParamSchema.getFilename() != null && !urlParamSchema.getFilename().isEmpty()) {
//                java.nio.file.Path path = Paths.get(System.getProperty("java.io.tmpdir"), urlParamSchema.getFilename());
//                if (Files.exists(path)) {
//                    return Files.move(path, path.getParent().resolve(path.getFileName().toString() + ".log"), StandardCopyOption.ATOMIC_MOVE);
//                }
//            }
//        }
//
//        String logFilename = "master.log";
//        checkRequiredParameter("jobschedulerId", urlParamSchema.getJobschedulerId());
//        try {
//            checkRequiredParameter("url", urlParamSchema.getUrl());
//        } catch (JocMissingRequiredParameterException e) {
//            if (dbItemInventoryInstance.getIsCluster()) {
//                throw e;
//            } else {
//                urlParamSchema.setUrl(URI.create(dbItemInventoryInstance.getUri()));
//            }
//        }
//
//        // increase timeout for large log files
//        int socketTimeout = Math.max(Globals.httpSocketTimeout, 30000);
//        JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParamSchema.getUrl(), getAccessToken());
//        jocJsonCommand.setSocketTimeout(socketTimeout);
//        jocJsonCommand.setUriBuilderForMainLog(true);
//        return jocJsonCommand.getFilePathFromGet(jocJsonCommand.getURI(), "sos-" + logFilename + "-download-", "text/plain,application/octet-stream",
//                true);
//    }

//    private long getSize(java.nio.file.Path path) throws IOException {
//        RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
//        raf.seek(raf.length() - 4);
//        int b4 = raf.read();
//        int b3 = raf.read();
//        int b2 = raf.read();
//        int b1 = raf.read();
//        raf.close();
//        return ((long) b1 << 24) | ((long) b2 << 16) | ((long) b3 << 8) | (long) b4;
//    }

}
