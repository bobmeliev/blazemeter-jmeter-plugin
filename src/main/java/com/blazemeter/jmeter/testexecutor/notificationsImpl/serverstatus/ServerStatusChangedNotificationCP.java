package com.blazemeter.jmeter.testexecutor.notificationsImpl.serverstatus;

import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.IServerStatusChangedNotification;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 12/24/13.
 */
public class ServerStatusChangedNotificationCP implements IServerStatusChangedNotification {
    private JButton runInTheCloud;

    public ServerStatusChangedNotificationCP(JButton runInTheCloud) {
        this.runInTheCloud = runInTheCloud;
    }

    @Override
    public void onServerStatusChanged() {
        ServerStatusController.ServerStatus serverStatus = ServerStatusController.getServerStatus();
        switch (serverStatus) {
            case AVAILABLE:
                TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
                boolean testIsRunning = testInfo.getStatus() == TestStatus.Running;
                runInTheCloud.setEnabled(!testIsRunning);
                break;
            case NOT_AVAILABLE:
                runInTheCloud.setEnabled(false);
                break;
        }

    }
}
