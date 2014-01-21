package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.IRunModeChangedNotification;
import com.blazemeter.jmeter.utils.BmLog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dzmitrykashlach on 12/26/13.
 */
public class RunModeListener implements ActionListener {
    private IRunModeChangedNotification runModeChangedNotification;

    public RunModeListener(IRunModeChangedNotification runModeChangedNotification) {
        this.runModeChangedNotification = runModeChangedNotification;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.setIsLocalRunMode(e.getActionCommand().equals("Locally (Reporting Only)"));
        boolean isLocalRunMode = bmTestManager.getIsLocalRunMode();
        runModeChangedNotification.onRunModeChanged(isLocalRunMode);
        BmLog.info(e.getActionCommand());

    }
}
