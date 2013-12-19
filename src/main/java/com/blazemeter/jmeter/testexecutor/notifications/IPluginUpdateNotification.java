package com.blazemeter.jmeter.testexecutor.notifications;

import com.blazemeter.jmeter.entities.PluginUpdate;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/21/13
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IPluginUpdateNotification {
    void onPluginUpdate(PluginUpdate update);
}
