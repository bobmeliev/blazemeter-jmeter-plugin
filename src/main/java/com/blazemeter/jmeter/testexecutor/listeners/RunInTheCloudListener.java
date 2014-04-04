package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.background.swingworkers.TestStarter;
import com.blazemeter.jmeter.background.swingworkers.TestStopper;
import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.dialogs.DialogFactory;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dzmitrykashlach on 12/24/13.
 */
public class RunInTheCloudListener implements ActionListener {
    private CloudPanel cloudPanel;

    public RunInTheCloudListener(CloudPanel cloudPanel) {
        this.cloudPanel = cloudPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int dialogButton;
        BmTestManager bmTestManager = BmTestManager.getInstance();
        if ("start".equals(e.getActionCommand().toLowerCase())) {
            if (bmTestManager.getUserKey().isEmpty()) {
                JMeterUtils.reportErrorToUser("Please, set up user key.", "User key is not set.");
                return;
            }
            dialogButton = JOptionPane.showConfirmDialog(TestPanel.getTestPanel().getMainPanel(), "Are you sure that you want to start the test?",
                    "Start test?",
                    JOptionPane.YES_NO_OPTION);
            if (dialogButton == JOptionPane.YES_OPTION) {
                TestStarter testStarter = new TestStarter(cloudPanel);
                testStarter.execute();
                testStarter.addPropertyChangeListener(DialogFactory.getStartProgressDialog(testStarter));
                testStarter.firePropertyChange(Constants.BUTTON_ACTION, SwingWorker.StateValue.PENDING,
                        SwingWorker.StateValue.STARTED);
            }
        } else {
            dialogButton = JOptionPane.showConfirmDialog(TestPanel.getTestPanel().getMainPanel(), "Are you sure that you want to stop the test? ",
                    "Stop test?",
                    JOptionPane.YES_NO_OPTION);
            if (dialogButton == JOptionPane.YES_OPTION) {
                TestStopper testStopper = new TestStopper(cloudPanel);
                testStopper.execute();
                testStopper.addPropertyChangeListener(DialogFactory.getStopProgressDialog(testStopper));
                testStopper.firePropertyChange(Constants.BUTTON_ACTION, SwingWorker.StateValue.STARTED,
                        SwingWorker.StateValue.DONE);
            }
        }
    }
}
