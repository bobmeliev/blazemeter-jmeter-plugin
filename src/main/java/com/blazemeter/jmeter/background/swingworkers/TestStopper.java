package com.blazemeter.jmeter.background.swingworkers;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class TestStopper extends SwingWorker {
    private CloudPanel cloudPanel;

    public TestStopper(CloudPanel cloudPanel) {
        this.cloudPanel = cloudPanel;
    }

    @Override
    protected Object doInBackground() throws Exception {
        BmTestManager.getInstance().stopInTheCloud();
        return null;
    }
}
