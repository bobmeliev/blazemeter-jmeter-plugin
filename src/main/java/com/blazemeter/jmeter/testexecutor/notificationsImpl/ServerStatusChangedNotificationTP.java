package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.controllers.TestInfoController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.IServerStatusChangedNotification;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;
import com.blazemeter.jmeter.testexecutor.panels.PropertyPanel;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import com.blazemeter.jmeter.utils.Utils;

/**
 * Created by dzmitrykashlach on 12/24/13.
 */
public class ServerStatusChangedNotificationTP implements IServerStatusChangedNotification {
    private TestPanel testPanel;
    private CloudPanel cloudPanel;
    private PropertyPanel jMeterPropertyPanel;

    public ServerStatusChangedNotificationTP(TestPanel testPanel, CloudPanel cloudPanel, PropertyPanel jMeterPropertyPanel) {
        this.testPanel = testPanel;
        this.cloudPanel = cloudPanel;
        this.jMeterPropertyPanel = jMeterPropertyPanel;

    }

    @Override
    public void onServerStatusChanged() {
        ServerStatusController.ServerStatus serverStatus = ServerStatusController.getServerStatus();
        switch (serverStatus) {
            case AVAILABLE:
                TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
                TestInfoController.start(testInfo.getId());
                boolean testIsRunning = testInfo.getStatus() == TestStatus.Running;
                testPanel.enableMainPanelControls(!testIsRunning);
                Utils.enableElements(cloudPanel, !testIsRunning);
                Utils.enableElements(jMeterPropertyPanel, !testIsRunning);
                break;
            case NOT_AVAILABLE:
                testPanel.enableMainPanelControls(false);
                Utils.enableElements(jMeterPropertyPanel, false);
                TestInfoController.stop();
                break;
        }
    }
}
