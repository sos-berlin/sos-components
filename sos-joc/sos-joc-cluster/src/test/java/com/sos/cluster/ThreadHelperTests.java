package com.sos.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sos.joc.cluster.ThreadHelper;

public class ThreadHelperTests {

    private static Runnable MyTask() {
        return () -> {

            for (int i = 0; i < 10; i++) {

                System.out.println(i);      // requirement 1

                Thread t = new Thread(MyTaskChild(), "childthread-");
                t.start();

                try {

                    Thread.sleep(1_000);  // requirement 2

                } catch (InterruptedException e) {

                    // break; // requirement 7

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

                    // break; // requirement 7

                }

            }

        };

    }

    class MyTimerTaskChild extends TimerTask {

        public void run() {
            System.out.println("MyTask task is running....");
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("MyTask task is stopped");

        }
    }

    public static void main(String[] args) throws Exception {

        ThreadHelperTests test = new ThreadHelperTests();

        Timer t1 = new Timer();
        t1.schedule(test.new MyTimerTaskChild(), 0, 1_000);

        Thread t = new Thread(MyTask(), "mainthread-");
        t.start();

        // Thread t2 = new Thread(MyTask());
        // t2.start();

        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "before");

        List<String> toStop = new ArrayList<String>();
        toStop.add("mainthread-");
        toStop.add("Timer-");

        ThreadHelper.stopThreads(toStop);
        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "after");
    }
}
