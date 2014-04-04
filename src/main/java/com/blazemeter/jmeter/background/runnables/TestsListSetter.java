package com.blazemeter.jmeter.background.runnables;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.TestInfoController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.GuiUtils;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
public class TestsListSetter implements Runnable {
    private JComboBox testIdComboBox;
    private CloudPanel cloudPanel;
    private JPanel mainPanel;
    private ArrayList<TestInfo> tests;
    private boolean silentMode;

    public TestsListSetter(JComboBox testIdComboBox,
                           CloudPanel cloudPanel,
                           JPanel mainPanel,
                           ArrayList<TestInfo> tests, boolean silentMode) {

        this.testIdComboBox = testIdComboBox;
        this.cloudPanel = cloudPanel;
        this.mainPanel = mainPanel;
        this.tests = tests;
        this.silentMode = silentMode;
    }


    @Override
    public void run() {
        if (tests != null) {
            BmTestManager bmTestManager = BmTestManager.getInstance();
            bmTestManager.setUserKeyValid(true);
            testIdComboBox.removeAllItems();
            testIdComboBox.setEnabled(true);
            testIdComboBox.addItem(Constants.NEW);
            List<String> testIdList = new ArrayList<String>();
            // create list of tests on server

            for (TestInfo ti : tests) {
                GuiUtils.addTestId(testIdComboBox, ti, false);
                testIdList.add(ti.getId());
            }
            String[] curTest = StringUtils.split(JMeterUtils.getPropDefault(Constants.CURRENT_TEST, ""), ";");
            String curTestId = null;

            try {
                if (curTest.length > 0) {
                    curTestId = curTest[0];
                }
            } catch (ArrayIndexOutOfBoundsException iobe) {
                BmLog.error("Current test property was not applied to screen: " + iobe);
            }

            // select current test(which was previously selected in testIdComboBox)
            testIdComboBox.setSelectedItem(Constants.NEW);
            if (curTest.length != 0) {
                for (TestInfo ti : tests) {
                    if (ti.getId().equals(curTestId)) {
                        testIdComboBox.setSelectedItem(ti);
                    }
                }
                if ((!testIdList.isEmpty() & !curTestId.isEmpty()) && !testIdList.contains(curTestId)
                        && !curTestId.equals(Constants.NEW)) {
                    JMeterUtils.reportErrorToUser("Test=" + curTestId + " was not found on server. Select test from list."
                            , "Test was not found on server");
                    JMeterUtils.setProperty(Constants.CURRENT_TEST, "");
                }
            }


        } else {
            BmTestManager.getInstance().setUserKeyValid(false);
            if (!silentMode) {
                testIdComboBox.removeAllItems();
                JOptionPane.showMessageDialog(mainPanel, "Please enter valid user key", "Invalid user key", JOptionPane.ERROR_MESSAGE);
                BmTestManager.getInstance().setUserKeyValid(false);
                cloudPanel.reset();
                Utils.enableElements(cloudPanel, false);
                testIdComboBox.setSelectedItem(Constants.EMPTY);
                TestPanel.getTestPanel().configureMainPanel(null);
                TestInfoController.stop();
            } else {
                BmLog.debug("Invalid userKey was found. Tests are not received from BM server");
            }
        }

    }
}