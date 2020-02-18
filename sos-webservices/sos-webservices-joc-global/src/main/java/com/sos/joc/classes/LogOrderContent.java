package com.sos.joc.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;

public class LogOrderContent extends LogContent {

    private Long historyId;
    private String prefix = "sos-%d.order.log-download-";

    public LogOrderContent(Long historyId, DBItemInventoryInstance dbItemInventoryInstance) {
        super();
        this.historyId = historyId;
    }

    public Path writeGzipLogFile() throws SOSHibernateException, JocConfigurationException, DBOpenSessionException, IOException,
            JobSchedulerObjectNotExistException, JocMissingRequiredParameterException, DBConnectionRefusedException, DBInvalidDataException {

        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'historyId'");
        }
        Path path = writeGzipLogFileFromDB();
        // TODO web service call for runnning logs
        if (path == null) {
            path = writeGzipLogFileFromHistoryService();
        }
        if (path == null) {
            String msg = String.format("Order log with id %s is missing", historyId);
            throw new JobSchedulerObjectNotExistException(msg);
        }
        return path;
    }

    private Path writeGzipLogFileFromDB() throws SOSHibernateException, IOException, JocConfigurationException, DBOpenSessionException,
            DBConnectionRefusedException, DBInvalidDataException {
        SOSHibernateSession connection = null;
        Path path = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./order/log");
            JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(connection);
            List<Long> logIds = dbLayer.getLogIdsFromOrder(historyId);
            if (logIds == null || logIds.isEmpty()) {
                return null;
            } else {
                for (Long logId : logIds) {
                    DBItemLog historyDBItem = connection.get(DBItemLog.class, logId);
                    if (historyDBItem == null) {
                        continue;
                    } else {
                        if (path == null) {
                            path = historyDBItem.writeGzipLogFile(String.format(prefix, historyId));
                        } else {
                            historyDBItem.writeGzipLogFile(path, true);
                        }
                    }
                }
                if (logIds.size() > 1) {
                    //TODO test if path is merged gzip
                    Path path2 = Files.createTempFile(String.format(prefix, historyId), null);
                    boolean unMerged = mergedGzipToFile(path, path2);
                    Files.deleteIfExists(path);
                    path = unMerged ? path2 : null;
                }
                return path;
            }
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private boolean mergedGzipToFile(Path source, Path target) throws IOException {
        if (source == null || target == null) {
            return false;
        }
        InputStream is = null;
        OutputStream out = null;
        try {
            is = new GzipCompressorInputStream(Files.newInputStream(source), true);
            out = new GZIPOutputStream(Files.newOutputStream(target));
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return true;
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
            }
        }
    }

    

    private Path writeGzipLogFileFromHistoryService() {
        // TODO Auto-generated method stub
        return null;
    }
}
