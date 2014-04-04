package com.blazemeter.jmeter.testexecutor.dialogs;

import com.blazemeter.jmeter.entities.TestStatus;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class DialogFactory {
    private static final String title = "Please, wait...";
    private static final String message = "Operation will take a few seconds to execute. Your patience is appreciated.";
    private static CloudTestProgressDialog startProgressDialog;
    private static CloudTestProgressDialog stopProgressDialog;
    private static OperationProgressDialog operationProgressDialog;

    public static OperationProgressDialog getStartProgressDialog() {
        if (startProgressDialog == null) {
            startProgressDialog = new CloudTestProgressDialog(title, message, TestStatus.Running);
        }
        return startProgressDialog;
    }

    public static OperationProgressDialog getStopProgressDialog() {
        if (stopProgressDialog == null) {
            stopProgressDialog = new CloudTestProgressDialog(title, message, TestStatus.NotRunning);
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
