package com.blazemeter.jmeter.testexecutor.dialogs;

import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;
import com.blazemeter.jmeter.testexecutor.notificationsImpl.testinfo.TestInfoNotificationProgrDial;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class DialogFactory {
    private static final String title = "Please, wait...";
    private static final String message = "Operation will take a few seconds to execute. Your patience is appreciated.";
    private static CloudTestProgressDialog startProgressDialog;
    private static CloudTestProgressDialog stopProgressDialog;
    private static OperationProgressDialog operationProgressDialog;

    public static OperationProgressDialog getStartProgressDialog(SwingWorker swingWorker) {
        if (startProgressDialog == null) {
            ITestInfoNotification testInfoNotification = new TestInfoNotificationProgrDial(swingWorker, TestStatus.NotRunning);
            startProgressDialog = new CloudTestProgressDialog(title, message, testInfoNotification);
        }
        return startProgressDialog;
    }

    public static OperationProgressDialog getStopProgressDialog(SwingWorker swingWorker) {
        if (stopProgressDialog == null) {
            ITestInfoNotification testInfoNotification = new TestInfoNotificationProgrDial(swingWorker, TestStatus.Running);
            stopProgressDialog = new CloudTestProgressDialog(title, message, testInfoNotification);
        }
        return stopProgressDialog;
    }


    public static OperationProgressDialog getOperationProgressDialog() {
        if (operationProgressDialog == null) {
            operationProgressDialog = new OperationProgressDialog(title, message);
        }
        return operationProgressDialog;
    }
}
