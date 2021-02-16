package com.sos.cluster;

import java.util.Timer;
import java.util.TimerTask;

import com.sos.joc.cluster.ThreadHelper;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;

public class ThreadHelperTests {

    private static Runnable MyTask(ThreadGroup tg) {
        return () -> {

            for (int i = 0; i < 10; i++) {

                System.out.println(i);      // requirement 1

                Thread t = new Thread(tg, MyTaskChild(), "childthread-");
                t.start();

                try {

                    Thread.sleep(1_000);  // requirement 2

                } catch (InterruptedException e) {

                    break; // requirement 7

                }

            }

        };

    }

    private static Runnable MyTaskChild() {
        return () -> {

            for (int i = 0; i < 5; i++) {

                System.out.println("child_" + i);      // requirement 1

                try {

                    Thread.sleep(1_000);  // requirement 2

                } catch (InterruptedException e) {

                    break; // requirement 7

                }

            }

        };

    }

    class MyTimerTaskChild extends TimerTask {

        public void run() {
            System.out.println("MyTimerTaskChild task is running....");
            try {
                Thread.sleep(10_000);
                System.out.println("MyTimerTaskChild task is stopped");
            } catch (InterruptedException e) {
                System.out.println("MyTimerTaskChild task is interrupted");
            }
        }
    }

    private void executeTimer() {
        Timer t1 = new Timer();
        // t1.schedule(test.new MyTimerTaskChild(), 0, 1_000);
        t1.schedule(new MyTimerTaskChild(), 0, 1_000);
    }

    public static void main(String[] args) throws Exception {

        try {
            ThreadHelperTests test = new ThreadHelperTests();
            test.executeTimer();

            ThreadGroup groupMain = new ThreadGroup("mymain");
            ThreadGroup groupMainChild = new ThreadGroup(groupMain, "child");

            Thread t1 = new Thread(groupMain, MyTask(groupMainChild), "groupMain-");
            t1.start();

            Thread.sleep(1_000);

            ThreadHelper.print(StartupMode.manual, "-------------------------");

            // ThreadHelperTests.interruptThreads();
            ThreadHelper.tryStop(StartupMode.manual, groupMain);

            ThreadHelper.tryStop(StartupMode.manual, "Timer");

            ThreadHelper.print(StartupMode.manual, "-------------------------");

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
