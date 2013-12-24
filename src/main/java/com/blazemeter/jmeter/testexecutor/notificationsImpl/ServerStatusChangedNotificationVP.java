package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.testexecutor.notifications.IServerStatusChangedNotification;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dzmitrykashlach on 12/24/13.
 */
public class ServerStatusChangedNotificationVP implements IServerStatusChangedNotification {
    private JLabel connectionStatus;

    public ServerStatusChangedNotificationVP(JLabel connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    @Override
    public void onServerStatusChanged() {
        ServerStatusController.ServerStatus serverStatus = ServerStatusController.getServerStatus();
        switch (serverStatus) {
            case AVAILABLE:
                connectionStatus.setText("SERVER IS AVAILABLE");
                connectionStatus.setForeground(Color.GREEN);
                break;
            case NOT_AVAILABLE:
                connectionStatus.setText("SERVER IS NOT AVAILABLE");
                connectionStatus.setForeground(Color.RED);
                break;
        }
    }
}
