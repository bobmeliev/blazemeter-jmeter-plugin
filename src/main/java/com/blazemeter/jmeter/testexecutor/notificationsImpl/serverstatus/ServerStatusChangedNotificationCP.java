package com.blazemeter.jmeter.testexecutor.notificationsImpl.serverstatus;

import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.IServerStatusChangedNotification;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;
import com.blazemeter.jmeter.utils.Utils;

/**
 * Created by dzmitrykashlach on 12/24/13.
 */
public class ServerStatusChangedNotificationCP implements IServerStatusChangedNotification {
    private CloudPanel cloudPanel;

    public ServerStatusChangedNotificationCP(CloudPanel cloudPanel) {
        this.cloudPanel = cloudPanel;
    }

    @Override
    public void onServerStatusChanged() {
        ServerStatusController.ServerStatus serverStatus = ServerStatusController.getServerStatus();
        switch (serverStatus) {
            case AVAILABLE:
                TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
                boolean testIsRunning = testInfo.getStatus() == TestStatus.Running;
                Utils.enableElements(cloudPanel, !testIsRunning);
                break;
            case NOT_AVAILABLE:
                Utils.enableElements(cloudPanel, false);
                break;
        }

    }
}
