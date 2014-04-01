package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.testexecutor.notifications.IRunModeChangedNotification;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;
import com.blazemeter.jmeter.utils.GuiUtils;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 12/26/13.
 */
public class RunModeChangedNotification implements IRunModeChangedNotification {
    private JRadioButton runLocal;
    private JRadioButton runRemote;
    private CloudPanel cloudPanel;
    private JTabbedPane advancedPropertiesPane;
    private boolean isLocalRunMode;

    public RunModeChangedNotification(JRadioButton runLocal, JRadioButton runRemote,
                                      CloudPanel cloudPanel, JTabbedPane advancedPropertiesPane) {
        this.runLocal = runLocal;
        this.runRemote = runRemote;
        this.cloudPanel = cloudPanel;
        this.advancedPropertiesPane = advancedPropertiesPane;
    }

    @Override
    public void onRunModeChanged(boolean isLocalRunMode) {
        GuiUtils.runModeChanged(runLocal, runRemote, cloudPanel, advancedPropertiesPane, isLocalRunMode);
    }
}
