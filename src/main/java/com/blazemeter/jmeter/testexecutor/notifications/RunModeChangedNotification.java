package com.blazemeter.jmeter.testexecutor.notifications;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/21/13
 * Time: 11:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RunModeChangedNotification {
    void onRunModeChanged(boolean isLocalRunMode);
}
