package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.utils.Utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/31/13
 * Time: 8:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class SaveUploadButtonListener implements ActionListener {
    /**
     * Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Utils.uploadJMX();
    }
}
