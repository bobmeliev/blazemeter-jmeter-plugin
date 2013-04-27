package com.blazemeter.jmeter.testinfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 4/26/13
 * Time: 9:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestInfoController {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private TestInfoController() {
    }

    public static void start(String testId) {
        final TestInfoChecker testInfoChecker = new TestInfoChecker(testId);
        if (scheduler.isShutdown()) {
            scheduler.scheduleAtFixedRate(testInfoChecker, 1, 30, TimeUnit.SECONDS);
        }
    }

    public static void stop() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}