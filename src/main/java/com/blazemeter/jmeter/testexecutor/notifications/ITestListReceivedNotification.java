package com.blazemeter.jmeter.testexecutor.notifications;

import com.blazemeter.jmeter.testinfo.TestInfo;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/27/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ITestListReceivedNotification {
    void testReceived(ArrayList<TestInfo> tests);
}
