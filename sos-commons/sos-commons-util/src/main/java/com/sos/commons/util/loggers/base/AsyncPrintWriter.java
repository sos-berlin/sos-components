package com.sos.commons.util.loggers.base;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** An asynchronous {@link java.io.PrintWriter} wrapper that processes write operations on a dedicated worker thread. */
public final class AsyncPrintWriter implements AutoCloseable {

    private static final long SHUTDOWN_TIMEOUT_MILLIS = 5_000;

    private enum Target {
        OUT, ERR
    }

    private static final class Message {

        private final Target target;
        private final String text;
        private final boolean stop;

        private Message(Target target, String text, boolean stop) {
            this.target = target;
            this.text = text;
            this.stop = stop;
        }

        static Message log(Target target, String text) {
            return new Message(target, text, false);
        }

        static Message stopMessage() {
            return new Message(null, null, true);
        }

        Target target() {
            return target;
        }

        String text() {
            return text;
        }

        boolean isStop() {
            return stop;
        }
    }

    private final PrintWriter out;
    private final PrintWriter err;
    private final LinkedBlockingQueue<Message> queue;
    private final Thread worker;

    private boolean accepting = true;

    // - Main Thread
    public AsyncPrintWriter(PrintWriter out, PrintWriter err, int queueCapacity, String threadName) {
        this.out = Objects.requireNonNull(out, "out");
        this.err = Objects.requireNonNull(err, "err");

        queue = initQueue(queueCapacity);

        worker = new Thread(this::writeLoop, threadName);
        worker.setDaemon(true);
        worker.start();
    }

    public Consumer<String> out() {
        return message -> submit(Target.OUT, message);
    }

    public Consumer<String> err() {
        return message -> submit(Target.ERR, message);
    }

    private LinkedBlockingQueue<Message> initQueue(int queueCapacity) {
        if (queueCapacity <= 0) {
            return new LinkedBlockingQueue<>();
        }
        return new LinkedBlockingQueue<>(queueCapacity);
    }

    private synchronized void submit(Target target, String text) {
        if (!accepting || text == null) {
            return;
        }
        queue.offer(Message.log(target, text));
    }

    // Worker Thread
    private void writeLoop() {
        try {
            while (true) {
                Message message = queue.take();
                if (message.isStop()) {
                    break;
                }

                PrintWriter writer = message.target() == Target.OUT ? out : err;
                try {
                    writer.println(message.text());
                } catch (Exception e) {
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        flush(out);
        if (err != out) {
            flush(err);
        }
    }

    private void flush(PrintWriter writer) {
        try {
            writer.flush();
        } catch (Exception e) {
        }
    }

    // Main Thread
    @Override
    public void close() {
        synchronized (this) {
            if (!accepting) {
                return;
            }
            accepting = false;
        }

        try {
            while (!queue.offer(Message.stopMessage(), 20, TimeUnit.MILLISECONDS)) {
                if (!worker.isAlive()) {
                    return;
                }
            }

            if (Thread.currentThread() != worker) {
                worker.join(SHUTDOWN_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
        }
    }

}
