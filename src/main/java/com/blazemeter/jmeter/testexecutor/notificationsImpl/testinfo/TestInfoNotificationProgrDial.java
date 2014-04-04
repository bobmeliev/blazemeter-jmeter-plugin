package com.blazemeter.jmeter.testexecutor.notificationsImpl.testinfo;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 4/4/14.
 */
public class TestInfoNotificationProgrDial implements ITestInfoNotification {
    private SwingWorker swingWorker = null;
    private TestStatus event = null;

    public TestInfoNotificationProgrDial(SwingWorker swingWorker,
                                         TestStatus event) {
        this.swingWorker = swingWorker;
        this.event = event;
    }

    @Override
    public void onTestInfoChanged(TestInfo testInfo) {
        TestStatus testStatus = testInfo.getStatus();
        if (testStatus != null && testStatus.equals(event)) {
            swingWorker.firePropertyChange(Constants.BUTTON_ACTION, SwingWorker.StateValue.STARTED,
                    SwingWorker.StateValue.DONE);
            BmTestManager.getInstance().getTestInfoNotificationListeners().remove(this);
        }
    }
}
