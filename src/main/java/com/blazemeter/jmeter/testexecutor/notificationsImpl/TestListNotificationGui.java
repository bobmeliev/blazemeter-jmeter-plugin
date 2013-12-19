package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.GuiUtils;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;

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
        JComboBox testIdComboBox = (JComboBox) applyNotificationTo.get(TEST_ID_COMBOBOX);
        JPanel mainPanel = (JPanel) applyNotificationTo.get(MAIN_PANEL);
        CloudPanel cloudPanel = (CloudPanel) applyNotificationTo.get(CLOUD_PANEL);
        testIdComboBox.removeAllItems();
        testIdComboBox.setEnabled(true);
        if (tests != null) {
            testIdComboBox.removeAllItems();
            testIdComboBox.addItem(Constants.NEW);
            testIdComboBox.setSelectedItem(Constants.NEW);
            BmTestManager.getInstance().setUserKeyValid(true);
            java.util.List<String> testIdList = new ArrayList<String>();
            // create list of tests on server
            for (TestInfo ti : tests) {
                GuiUtils.addTestId(testIdComboBox, ti, false);
                testIdList.add(ti.getId());
            }
            String[] curTest = StringUtils.split(JMeterUtils.getPropDefault(Constants.CURRENT_TEST, ""), ";");
            String curTestId = null;
            String curTestName = null;
            try {
                if (curTest.length > 0) {
                    curTestId = curTest[0];
                    curTestName = curTest[1];

                }

            } catch (ArrayIndexOutOfBoundsException iobe) {
                BmLog.error("Current test property was not applied to screen: " + iobe);
            }

            boolean exists = false;

            if (curTestId != null) {
                for (int index = 1; index <= testIdComboBox.getItemCount() && !exists; index++) {
                    Object obj = testIdComboBox.getItemAt(index);
                    if (obj instanceof TestInfo & obj != null) {
                        TestInfo ti = (TestInfo) testIdComboBox.getItemAt(index);
                        exists = curTestId.equals(ti.getId());
                    }
                }
            }
            //add current test to testIdComboBox if it is present in tests from server
            if (!exists & testIdList.contains(curTestId)) {
                testIdComboBox.addItem(curTestId + " - " + curTestName);
            }

            // select current test(which was previously selected in testIdComboBox)
            if (curTest.length != 0) {
                for (TestInfo ti : tests) {
                    if (ti.getId().equals(curTestId)) {
                        testIdComboBox.setSelectedItem(ti);
                    }
                }
                if ((!testIdList.isEmpty() & !curTestId.isEmpty()) && !testIdList.contains(curTestId)) {
                    JMeterUtils.reportErrorToUser("Test=" + curTestId + " was not found on server. Select test from list."
                            , "Test was not found on server");
                    JMeterUtils.setProperty(Constants.CURRENT_TEST, "");
                }
            }
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Please enter valid user key", "Invalid user key", JOptionPane.ERROR_MESSAGE);
            BmTestManager.getInstance().setUserKeyValid(false);
            cloudPanel.reset();
            Utils.enableElements(cloudPanel, false);
            testIdComboBox.setSelectedItem(Constants.EMPTY);
        }
    }
}
