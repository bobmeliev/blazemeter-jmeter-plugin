package com.blazemeter.jmeter.testexecutor.dialogs;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 4/4/14.
 */
public class CloudTestProgressDialog extends OperationProgressDialog {
    TestStatus event = null;

    public CloudTestProgressDialog(String title, String message, TestStatus event) {
        super(title, message);
        this.event = event;
        BmTestManager.getInstance().testInfoNotificationListeners.add(new ITestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                TestStatus testStatus = testInfo.getStatus();
                if (testStatus != null && testStatus.equals(CloudTestProgressDialog.this.event)) {
                    firePropertyChange(Constants.BUTTON_ACTION, SwingWorker.StateValue.STARTED,
                            SwingWorker.StateValue.DONE);
                }
            }
        });
    }
}
