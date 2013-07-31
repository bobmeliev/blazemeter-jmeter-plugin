package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

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
        try {
            GuiPackage guiPackage = GuiPackage.getInstance();
            if (guiPackage.getTestPlanFile() == null | guiPackage.isDirty()) {
                Utils.saveJMX(guiPackage);
            }
            BmTestManager.getInstance().uploadJmx();

        } catch (NullPointerException npe) {
            JMeterUtils.reportErrorToUser("Save test-plan locally before uploading");
        }

    }
}
