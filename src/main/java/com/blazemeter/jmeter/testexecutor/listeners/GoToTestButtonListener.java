package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.GuiUtils;
import org.apache.jmeter.util.JMeterUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dzmitrykashlach on 12/20/13.
 */
public class GoToTestButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String url = BmTestManager.getInstance().getTestUrl();
        if (url != null) {
            GuiUtils.Navigate(url);
        } else {
            JMeterUtils.reportErrorToUser("Test is not selected. Nothing to open.");
        }
    }
}
