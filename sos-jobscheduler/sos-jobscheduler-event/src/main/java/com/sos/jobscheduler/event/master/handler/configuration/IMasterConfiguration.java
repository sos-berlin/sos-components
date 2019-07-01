package com.sos.jobscheduler.event.master.handler.configuration;

public interface IMasterConfiguration {

    Master getBackup();

    Master getCurrent();

    void setCurrent(Master master);

    Master getPrimary();

    int getNotifyIntervalOnConnectionRefused();
}
