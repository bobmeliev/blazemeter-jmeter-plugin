package com.blazemeter.jmeter.testexecutor.notifications;

import com.blazemeter.jmeter.entities.TestInfo;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/21/13
 * Time: 11:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ITestInfoNotification {
    void onTestInfoChanged(TestInfo testInfo);
}
