package com.sos.js7.job;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.order.JOutcome;
import js7.data_for_java.order.JOutcome.Completed;
import js7.launcher.forjava.internal.BlockingInternalJob;

public class EmptyJobExample implements BlockingInternalJob {

    public EmptyJobExample(JobContext jobContext) {

    }

    public void onStart() throws Exception {

    }

    public void onStop() throws Exception {
    }

    @Override
    public Either<Problem, Void> start() {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("xyz");
            /// binding ...
            // JobContext.executable().script();
            // new JS Instance();
            engine.eval("js instance onStart());");
            onStart();
            return right(null);
        } catch (Throwable e) {
            return left(Problem.fromThrowable(e));
        }
    }

    @Override
    public void stop() {
        try {
            onStop();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public OrderProcess toOrderProcess(Step arg0) {
        return new OrderProcess() {

            @Override
            public Completed run() throws Exception {
                // onOrderProcess(xxx)
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("xyz");
                /// binding ...
                engine.eval("js class onOrderProcess();");

                return JOutcome.succeeded();// failed
            }
        };
    }

}
