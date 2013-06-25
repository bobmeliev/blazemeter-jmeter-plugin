package com.blazemeter.jmeter.testinfo;

import com.blazemeter.jmeter.utils.BmLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 4/26/13
 * Time: 9:53 AM
 * To change this template use File | Settings | File Templates.
 */

public class TestInfoController {
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> task;

    private TestInfoController() {
    }

    public static void start(String testId) {
        if ((task == null || task.isDone())&!testId.isEmpty()) {
            final TestInfoChecker testInfoChecker = new TestInfoChecker(testId);
            task = scheduler.scheduleAtFixedRate(testInfoChecker, 1, 30, TimeUnit.SECONDS);
            BmLog.console("TestInfoController is started with test.id=" + testId);
        }
    }

    public static void stop() {
        if (task != null && !task.isDone()) {
            task.cancel(true);
            task = null;
            BmLog.console("TestInfoController is stopped");

        }
    }
}