package com.blazemeter.jmeter.testexecutor.dialogs;

import com.blazemeter.jmeter.entities.TestStatus;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class DialogFactory {
    private static OperationProgressDialog startProgressDialog;
    private static OperationProgressDialog stopProgressDialog;

    public static OperationProgressDialog getStartProgressDialog() {
        if (startProgressDialog == null) {
            startProgressDialog = new OperationProgressDialog("Please, wait...",
                    "Operation will take a few seconds to execute. Your patience is appreciated.",
                    TestStatus.Running);
        }
        return startProgressDialog;
    }

    public static OperationProgressDialog getStopProgressDialog() {
        if (stopProgressDialog == null) {
            stopProgressDialog = new OperationProgressDialog("Please, wait...",
                    "Operation will take a few seconds to execute. Your patience is appreciated.",
                    TestStatus.NotRunning);
        }
        return stopProgressDialog;
    }
}
