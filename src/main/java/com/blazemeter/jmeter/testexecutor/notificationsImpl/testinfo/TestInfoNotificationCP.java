package com.blazemeter.jmeter.testexecutor.notificationsImpl.testinfo;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;
import com.blazemeter.jmeter.utils.Utils;

/**
 * Created by dzmitrykashlach on 12/23/13.
 */
public class TestInfoNotificationCP implements ITestInfoNotification {
    private CloudPanel cloudPanel;

    public TestInfoNotificationCP(CloudPanel cloudPanel) {
        this.cloudPanel = cloudPanel;
    }

    @Override
    public void onTestInfoChanged(TestInfo testInfo) {
        if (testInfo == null) {
            return;
        }

        if (testInfo.getStatus() == TestStatus.Running) {
            Utils.enableElements(this.cloudPanel, false);
            cloudPanel.getRunInTheCloud().setEnabled(true);
        }

        if ((testInfo.getStatus() == TestStatus.NotRunning)) {
            boolean isTestIdEmpty = testInfo.getId().isEmpty();
            Utils.enableElements(this.cloudPanel, !isTestIdEmpty);
            cloudPanel.getRunInTheCloud().setEnabled(!isTestIdEmpty);
        }
        cloudPanel.setTestInfo(testInfo);
    }
}
