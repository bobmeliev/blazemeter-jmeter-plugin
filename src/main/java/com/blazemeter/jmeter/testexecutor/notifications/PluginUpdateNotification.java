package com.blazemeter.jmeter.testexecutor.notifications;

import com.blazemeter.jmeter.utils.PluginUpdate;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/21/13
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PluginUpdateNotification {
    void onPluginUpdate(PluginUpdate update);
}
