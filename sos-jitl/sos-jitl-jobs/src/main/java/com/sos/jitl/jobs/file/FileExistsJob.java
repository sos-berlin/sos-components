package com.sos.jitl.jobs.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSDate;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.OrderProcessStep;
import com.sos.jitl.jobs.common.OrderProcessStepLogger;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobFileExistsArguments;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

public class FileExistsJob extends ABlockingInternalJob<FileOperationsJobFileExistsArguments> {

    public FileExistsJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<FileOperationsJobFileExistsArguments> step) throws Exception {
        AFileOperationsJob.checkArguments(step.getDeclaredArguments());

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        FileOperationsJobFileExistsArguments args = step.getDeclaredArguments();
        boolean result = fo.existsFile(new File(args.getSourceFile().getValue()), args.getFileSpec().getValue(), args.getRecursive().getValue(),
                Pattern.CASE_INSENSITIVE, args.getMinFileAge().getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args
                        .getMaxFileSize().getValue(), args.getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue());
        if (result) {
            result = checkSteadyStateOfFiles(step.getLogger(), args, fo.getResultList());
        }
        AFileOperationsJob.handleResult(step, fo.getResultList(), result);
    }

    public boolean checkSteadyStateOfFiles(OrderProcessStepLogger logger, FileOperationsJobFileExistsArguments args, List<File> files)
            throws Exception {
        if (files == null || files.size() == 0 || args.getSteadyStateCount().getValue() <= 0) {
            return true;
        }
        Integer interval = args.getSteadyStateInterval().getValue();
        if (interval == null || interval <= 0) {
            logger.debug("skip checking file(s) for steady state, interval=%ss", interval);
            return true;
        }

        logger.debug("checking file(s) for steady state, interval=%ss", interval);
        List<FileDescriptor> list = new ArrayList<FileDescriptor>();
        for (File file : files) {
            list.add(new FileDescriptor(file));
        }
        try {
            TimeUnit.SECONDS.sleep(interval.longValue());
        } catch (InterruptedException e) {
            logger.error(e.toString(), e);
        }

        boolean result = true;
        for (int i = 0; i < args.getSteadyStateCount().getValue(); i++) {
            result = true;
            for (FileDescriptor fd : list) {
                File file = new File(fd.getFileName());
                if (logger.isDebugEnabled()) {
                    logger.debug("[steady state][%s][modified last=%s current=%s][file length last=%sb current=%sb]", file.getCanonicalPath(), SOSDate
                            .getDateTimeAsString(fd.getLastModificationDate()), SOSDate.getDateTimeAsString(file.lastModified()), fd
                                    .getLastFileLength(), file.length());
                }
                if (args.getUseFileLock().getValue()) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(file, "rw");
                        FileChannel channel = raf.getChannel();
                        FileLock lock = channel.lock();
                        try {
                            lock = channel.tryLock();
                            logger.debug(String.format("lock for file '%1$s' ok", file.getAbsolutePath()));
                            break;
                        } catch (OverlappingFileLockException e) {
                            result = false;
                            logger.info(String.format("File '%s' is open by someone else", file.getAbsolutePath()));
                            break;
                        } finally {
                            lock.release();
                            logger.debug(String.format("release lock for '%s'", file.getAbsolutePath()));
                            if (raf != null) {
                                channel.close();
                                raf.close();
                                raf = null;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (file.lastModified() != fd.getLastModificationDate() || file.length() != fd.getLastFileLength()) {
                    result = false;
                    fd.update(file);
                    fd.setSteady(false);
                    logger.info(String.format("File '%s' changed during checking steady state", file.getAbsolutePath()));
                    break;
                } else {
                    fd.setSteady(true);
                }
            }
            if (!result) {
                try {
                    TimeUnit.SECONDS.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error(e.toString(), e);
                }
            } else {
                break;
            }
        }
        if (!result) {
            logger.error("not all files are steady");
            for (FileDescriptor fd : list) {
                if (!fd.isSteady()) {
                    logger.info(String.format("File '%s' is not steady", fd.getFileName()));
                }
            }
            throw new SOSFileOperationsException("not all files are steady");
        }
        return result;
    }

    private class FileDescriptor {

        private long lastModificationDate;
        private long lastFileLength;
        private String fileName;
        private boolean isSteady;

        private FileDescriptor(final File file) {
            lastFileLength = file.length();
            lastModificationDate = file.lastModified();
            fileName = file.getAbsolutePath();
        }

        private void update(final File file) {
            lastFileLength = file.length();
            lastModificationDate = file.lastModified();
        }

        private long getLastModificationDate() {
            return lastModificationDate;
        }

        private long getLastFileLength() {
            return lastFileLength;
        }

        private String getFileName() {
            return fileName;
        }

        private boolean isSteady() {
            return isSteady;
        }

        private void setSteady(boolean val) {
            isSteady = val;
        }

    }

}
