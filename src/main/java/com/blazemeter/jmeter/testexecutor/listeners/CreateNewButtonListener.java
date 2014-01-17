package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;
import com.blazemeter.jmeter.utils.GuiUtils;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

/**
 * Created by dzmitrykashlach on 12/20/13.
 */
public class CreateNewButtonListener implements ActionListener {
    JTextArea testNameArea;
    JPanel mainPanel;
    CloudPanel cloudPanel;
    JComboBox testIdComboBox;

    public CreateNewButtonListener(JTextArea testNameArea, JPanel mainPanel, CloudPanel cloudPanel, JComboBox testIdComboBox) {
        this.testNameArea = testNameArea;
        this.mainPanel = mainPanel;
        this.cloudPanel = cloudPanel;
        this.testIdComboBox = testIdComboBox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        String userKey = bmTestManager.getUserKey();
        if (userKey == null || userKey.isEmpty()) {
            JMeterUtils.reportErrorToUser("Please enter user key", "No user key");
            bmTestManager.setUserKeyValid(false);
            return;

        }
        String testName = testNameArea.getText().trim();
        if (testName.isEmpty() | testName.equals(Constants.NEW)) {
            testName = JOptionPane.showInputDialog(mainPanel, "Please enter valid test name!");
            if (testName == null || testName.trim().isEmpty())
                return;
        }
        if (Utils.isTestPlanEmpty()) {
            JMeterUtils.reportErrorToUser("Test-plan should contain at least one Thread Group");
            return;
        }
        int numberOfUsers = cloudPanel.getNumberOfUsers();
        TestInfo ti = bmTestManager.createTest(userKey, testName);
        ti.setLocation(cloudPanel.getServerLocation());
        ti.setNumberOfUsers(numberOfUsers);
        ti.setStatus(TestStatus.NotRunning);
        Properties jmeterProperties = null;

        if (!bmTestManager.getIsLocalRunMode()) {
            jmeterProperties = bmTestManager.getTestInfo().getJmeterProperties();

        } else {
            jmeterProperties = new Properties();
        }

        ti.setJmeterProperties(jmeterProperties);
        ti = bmTestManager.updateTestSettings(userKey, ti);
        if (ti != null && ti.getStatus() != null) {
            GuiUtils.addTestId(testIdComboBox, ti, true);
            bmTestManager.setTestInfo(ti);
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage.getTestPlanFile() == null) {

            Utils.saveJMX();
        }
        bmTestManager.uploadJmx();

    }
}
