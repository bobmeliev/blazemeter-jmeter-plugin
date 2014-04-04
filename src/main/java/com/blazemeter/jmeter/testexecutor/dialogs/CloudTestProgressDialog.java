package com.blazemeter.jmeter.testexecutor.dialogs;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;

/**
 * Created by dzmitrykashlach on 4/4/14.
 */
public class CloudTestProgressDialog extends OperationProgressDialog {

    public CloudTestProgressDialog(String title, String message, ITestInfoNotification testInfoNotification) {
        super(title, message);
        BmTestManager.getInstance().getTestInfoNotificationListeners().add(testInfoNotification);
    }
}
