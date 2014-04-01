package com.blazemeter.jmeter.testexecutor.dialogs;

import com.blazemeter.jmeter.entities.TestStatus;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class DialogFactory {
    private static OperationProgressDialog startProgressDialog;
    private static OperationProgressDialog stopProgressDialog;

    private static final String title = "Please, wait...";
    private static final String message = "Operation will take a few seconds to execute. Your patience is appreciated.";

    public static OperationProgressDialog getStartProgressDialog() {
        if (startProgressDialog == null) {
            startProgressDialog = new OperationProgressDialog(title, message, TestStatus.Running);
        }
        return startProgressDialog;
    }

    public static OperationProgressDialog getStopProgressDialog() {
        if (stopProgressDialog == null) {
            stopProgressDialog = new OperationProgressDialog(title, message, TestStatus.NotRunning);
        }
        return stopProgressDialog;
    }
}
