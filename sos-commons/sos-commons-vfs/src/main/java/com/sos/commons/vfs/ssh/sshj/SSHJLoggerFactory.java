package com.sos.commons.vfs.ssh.sshj;

import java.util.Set;

import org.slf4j.Logger;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.DelegatingSlf4jLogger;

import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;

public class SSHJLoggerFactory implements LoggerFactory {

    /** Logger names for which TRACE logging is suppressed.<br />
     * Encoder and Decoder are specified by name because their classes are final. */
    private static final Set<String> TRACE_BLOCKED = Set.of("net.schmizz.sshj.transport.Encoder", "net.schmizz.sshj.transport.Decoder",
            SessionChannel.class.getName());

    private final Logger logger;
    private final Logger nonTraceLogger;

    public SSHJLoggerFactory(ISOSLogger delegate, boolean traceUnfiltered, boolean async) {
        this.logger = new DelegatingSlf4jLogger(delegate, delegate.isDebugEnabled(), delegate.isTraceEnabled(), async);
        this.nonTraceLogger = delegate.isTraceEnabled() && !traceUnfiltered ? new DelegatingSlf4jLogger(delegate, delegate.isDebugEnabled(), false,
                async) : null;
    }

    @Override
    public Logger getLogger(String name) {
        if (nonTraceLogger != null && TRACE_BLOCKED.contains(name)) {
            return nonTraceLogger;
        }
        return logger;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

}
