package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.Utils;
import com.blazemeter.jmeter.utils.background.JMXUploader;
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
            BmTestManager bmTestManager = BmTestManager.getInstance();
            GuiPackage guiPackage = GuiPackage.getInstance();
            if (Utils.isTestPlanEmpty()) {
                JMeterUtils.reportErrorToUser("Test-plan should have at least one Thread Group");
                return;
            }
            if (guiPackage.getTestPlanFile() == null | guiPackage.isDirty()) {
                Utils.saveJMX();
            }
            TestInfo testInfo = bmTestManager.getTestInfo();
            if (testInfo.getNumberOfUsers() == 0) {
                JMeterUtils.reportErrorToUser("Can't set up test with 0 users. " +
                        " '1' will be saved");
                testInfo.setNumberOfUsers(1);
            }
            bmTestManager.updateTestSettings(bmTestManager.getUserKey(), bmTestManager.getTestInfo());
            JMXUploader.uploadJMX();
        } catch (NullPointerException npe) {
            JMeterUtils.reportErrorToUser("Save test-plan locally before uploading");
        }

    }
}
