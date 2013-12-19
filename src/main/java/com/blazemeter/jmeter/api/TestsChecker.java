package com.blazemeter.jmeter.api;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;

import java.util.ArrayList;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
public class TestsChecker implements Runnable {
    String userKey;
    ITestListReceivedNotification notifier;

    public TestsChecker(String userKey, ITestListReceivedNotification notifier) {
        this.userKey = userKey;
        this.notifier = notifier;
    }

    public void run() {
        ArrayList<TestInfo> tests = BlazemeterApi.getInstance().getTests(userKey);
        notifier.testReceived(tests);
    }
}
