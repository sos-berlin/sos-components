package com.sos.js7.converter.js1.common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.job.ACommonJob;
import com.sos.js7.converter.js1.common.job.OrderJob;
import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.jobchain.JobChain;
import com.sos.js7.converter.js1.common.lock.Lock;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public class Folder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Folder.class);

    private final Path path;
    private final Path parent;

    private List<Folder> folders = new ArrayList<>();
    private List<JobChain> jobChains = new ArrayList<>();
    private List<StandaloneJob> standaloneJobs = new ArrayList<>();
    private List<OrderJob> orderJobs = new ArrayList<>();

    private List<Lock> locks = new ArrayList<>();
    private List<ProcessClass> processClasses = new ArrayList<>();
    private List<Schedule> schedules = new ArrayList<>();
    private List<Monitor> monitors = new ArrayList<>();
    private List<Path> files = new ArrayList<>();

    public Folder(Path path) {
        this.path = path;
        this.parent = path.getParent();
    }

    public Folder findParent(Path path) {
        Path parent = path.getParent();
        if (this.path.equals(parent)) {
            return this;
        }

        Folder pf = null;
        x: for (Folder f : folders) {
            pf = f.findParent(path);
            if (pf != null) {
                break x;
            }
        }
        return pf;
    }

    public void addFolder(Folder folder) {
        folders.add(folder);
    }

    public void addJob(DirectoryParserResult pr, Path file) {
        ACommonJob job;
        try {
            job = ACommonJob.parse(pr, file);
            if (job != null) {
                if (job instanceof StandaloneJob) {
                    standaloneJobs.add((StandaloneJob) job);
                } else {
                    orderJobs.add((OrderJob) job);
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", "addJob", file, e.toString()), e);
            ParserReport.INSTANCE.addErrorRecord(file, null, e);
        }
    }

    public void addJobChain(DirectoryParserResult pr, String name, List<Path> jobChainFiles) throws Exception {
        jobChains.add(new JobChain(pr, name, jobChainFiles));
    }

    public void addLock(Path file) throws Exception {
        locks.add(new Lock(file));
    }

    public void addProcessClass(Path processClass) throws Exception {
        processClasses.add(new ProcessClass(processClass));
    }

    public void addSchedule(DirectoryParserResult pr, Path schedule) throws Exception {
        schedules.add(new Schedule(pr, schedule));
    }

    public void addMonitor(Monitor monitor) {
        monitors.add(monitor);
    }

    public void addFile(Path file) {
        files.add(file);
    }

    public Path getPath() {
        return path;
    }

    public Path getParent() {
        return parent;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public List<JobChain> getJobChains() {
        return jobChains;
    }

    public List<StandaloneJob> getStandaloneJobs() {
        return standaloneJobs;
    }

    public List<OrderJob> getOrderJobs() {
        return orderJobs;
    }

    public List<Lock> getLocks() {
        return locks;
    }

    public List<ProcessClass> getProcessClasses() {
        return processClasses;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public List<Path> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("path=").append(path);
        sb.append(",(sub)folders=").append(folders.size());
        if (standaloneJobs.size() > 0) {
            sb.append(",standaloneJobs=").append(standaloneJobs.size());
        }
        if (jobChains.size() > 0) {
            sb.append(",jobChains=").append(jobChains.size());
        }
        if (orderJobs.size() > 0) {
            sb.append(",orderJobs=").append(orderJobs.size());
        }
        if (locks.size() > 0) {
            sb.append(",locks=").append(locks.size());
        }
        if (processClasses.size() > 0) {
            sb.append(",processClasses=").append(processClasses.size());
        }
        if (schedules.size() > 0) {
            sb.append(",schedules=").append(schedules.size());
        }
        if (monitors.size() > 0) {
            sb.append(",monitors=").append(monitors.size());
        }
        if (files.size() > 0) {
            sb.append(",files=").append(files.size());
        }
        return sb.toString();
    }

}
