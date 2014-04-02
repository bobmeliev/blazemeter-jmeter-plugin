package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;
import com.blazemeter.jmeter.utils.background.TestsListSetter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
/*
This class defines actions upon receiving list of tests from BM server
 */
public class TestListNotification implements ITestListReceivedNotification {
    private HashMap<String, Object> applyNotificationTo;
    private boolean silent;

    public static final String TEST_ID_COMBOBOX = "testIdComboBox";
    public static final String MAIN_PANEL = "mainPanel";
    public static final String CLOUD_PANEL = "cloudPanel";


    public TestListNotification(HashMap<String, Object> applyNotificationTo, boolean silent) {
        this.applyNotificationTo = applyNotificationTo;
        this.silent = silent;
    }

    @Override
    public void testReceived(ArrayList<TestInfo> tests) {
        final JComboBox testIdComboBox = (JComboBox) applyNotificationTo.get(TEST_ID_COMBOBOX);
        final JPanel mainPanel = (JPanel) applyNotificationTo.get(MAIN_PANEL);
        final CloudPanel cloudPanel = (CloudPanel) applyNotificationTo.get(CLOUD_PANEL);
        TestsListSetter testsListSetter = new TestsListSetter(testIdComboBox, cloudPanel, mainPanel, tests, silent);
        SwingUtilities.invokeLater(testsListSetter);
    }
}
