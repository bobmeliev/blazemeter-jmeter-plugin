package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import com.blazemeter.jmeter.utils.Utils;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
public class TestIdComboBoxListener implements ItemListener {
    JComboBox testIdComboBox;
    CloudPanel cloudPanel;

    public TestIdComboBoxListener(JComboBox testIdComboBox, CloudPanel cloudPanel) {
        this.testIdComboBox = testIdComboBox;
        this.cloudPanel = cloudPanel;
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
            Object selectedTest = testIdComboBox.getSelectedItem();
            if (selectedTest instanceof TestInfo) {
                TestInfo testInfo = (TestInfo) selectedTest;
                if (!testInfo.getName().equals(Constants.NEW) & !testInfo.getName().isEmpty() & bmTestManager.isUserKeyValid()) {
                    bmTestManager.setTestInfo(testInfo);
                }
            } else if (selectedTest.toString().equals(Constants.NEW)) {
                testIdComboBox.setSelectedItem(Constants.NEW);
                TestPanel.getTestPanel().configureMainPanel(null);
                cloudPanel.reset();
                Utils.enableElements(cloudPanel, false);
                TestInfo testInfo = new TestInfo();
                testInfo.setName(Constants.NEW);
                bmTestManager.setTestInfo(testInfo);
            }
        }
    }
}
