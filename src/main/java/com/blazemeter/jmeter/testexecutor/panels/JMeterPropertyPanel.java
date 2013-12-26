package com.blazemeter.jmeter.testexecutor.panels;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;

import java.awt.event.ActionEvent;

/**
 * Created by dzmitrykashlach on 12/26/13.
 */
public class JMeterPropertyPanel extends PropertyPanel {
    @Override
    public void actionPerformed(ActionEvent action) {
        super.actionPerformed(action);
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.getTestInfo().setJmeterProperties(getData());
    }

    @Override
    protected void init() {
        super.init();
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.testInfoNotificationListeners.add(new ITestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                if (testInfo.getJmeterProperties() == null) {
                    testInfo.setJmeterProperties(getData());
                }

            }

        });
    }
}