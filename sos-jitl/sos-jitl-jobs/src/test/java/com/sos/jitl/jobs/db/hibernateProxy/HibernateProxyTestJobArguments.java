package com.sos.jitl.jobs.db.hibernateProxy;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class HibernateProxyTestJobArguments extends JobArguments {

    private JobArgument<Integer> factoryInstances = new JobArgument<>("factory_instances", false, Integer.valueOf(1));
    private JobArgument<Integer> inserts = new JobArgument<>("inserts", false, Integer.valueOf(0));

    public JobArgument<Integer> getFactoryInstances() {
        return factoryInstances;
    }

    public JobArgument<Integer> getInserts() {
        return inserts;
    }

}
