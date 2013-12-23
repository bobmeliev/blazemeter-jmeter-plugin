package com.blazemeter.jmeter.controllers;

import com.blazemeter.jmeter.api.checkers.TestsChecker;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.utils.BmLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
public class TestListController {
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> task;
    private static ITestListReceivedNotification notification;

    private TestListController() {
    }

    public static void setNotification(ITestListReceivedNotification notification) {
        TestListController.notification = notification;
    }

    public static void start(String userKey) {
        if ((task == null || task.isDone()) & !userKey.isEmpty()) {
            final TestsChecker testsChecker = new TestsChecker(userKey, notification);
            task = scheduler.scheduleAtFixedRate(testsChecker, 30, 30, TimeUnit.SECONDS);
            BmLog.console("TestListController is started with userKey=" + userKey);
        }
    }

    public static void stop() {
        if (task != null && !task.isDone()) {
            task.cancel(true);
            task = null;
            BmLog.console("TestListController is stopped");
        }
    }

}