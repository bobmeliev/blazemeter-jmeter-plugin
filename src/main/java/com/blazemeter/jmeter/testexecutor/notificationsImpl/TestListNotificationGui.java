package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
public class TestListNotificationGui implements ITestListReceivedNotification {
    private HashMap<String, Object> applyNotificationTo;

    public static final String TEST_ID_COMBOBOX = "testIdComboBox";
    public static final String MAIN_PANEL = "mainPanel";
    public static final String CLOUD_PANEL = "cloudPanel";

    public TestListNotificationGui(HashMap<String, Object> applyNotificationTo) {
        this.applyNotificationTo = applyNotificationTo;
    }

    @Override
    public void testReceived(ArrayList<TestInfo> tests) {
        final JComboBox testIdComboBox = (JComboBox) applyNotificationTo.get(TEST_ID_COMBOBOX);
        final JPanel mainPanel = (JPanel) applyNotificationTo.get(MAIN_PANEL);
        final CloudPanel cloudPanel = (CloudPanel) applyNotificationTo.get(CLOUD_PANEL);
        TestsListSetter testsListSetter = new TestsListSetter(testIdComboBox, cloudPanel, mainPanel, tests);
        SwingUtilities.invokeLater(testsListSetter);
    }
}
