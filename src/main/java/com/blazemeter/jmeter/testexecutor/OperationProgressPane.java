package com.blazemeter.jmeter.testexecutor;

import javafx.scene.control.ProgressBar;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/2/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class OperationProgressPane extends JOptionPane {
    private static JProgressBar jprogressBar = new JProgressBar();

    public OperationProgressPane() {
        super();
        this.add(jprogressBar);
        jprogressBar.setIndeterminate(true);
    }
}