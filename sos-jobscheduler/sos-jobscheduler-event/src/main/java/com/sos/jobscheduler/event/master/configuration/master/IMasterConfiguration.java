package com.sos.jobscheduler.event.master.configuration.master;

public interface IMasterConfiguration {

    Master getBackup();

    Master getCurrent();

    void setCurrent(Master master);

    Master getPrimary();
}
