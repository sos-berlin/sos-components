package com.sos.commons.vfs.ssh.sshj;

import org.slf4j.Logger;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.DelegatingSlf4jLogger;

import net.schmizz.sshj.common.LoggerFactory;

public class SSHJLoggerFactory implements LoggerFactory {

    private final Logger logger;

    public SSHJLoggerFactory(ISOSLogger delegate, boolean async) {
        this.logger = new DelegatingSlf4jLogger(delegate, delegate.isDebugEnabled(), async);
    }

    @Override
    public Logger getLogger(String name) {
        return logger;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return logger;
    }

}
