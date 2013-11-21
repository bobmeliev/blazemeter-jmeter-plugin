package com.blazemeter.jmeter.testexecutor.notifications;

import com.blazemeter.jmeter.testinfo.TestInfo;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/21/13
 * Time: 11:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TestInfoNotification {
    void onTestInfoChanged(TestInfo testInfo);
}
