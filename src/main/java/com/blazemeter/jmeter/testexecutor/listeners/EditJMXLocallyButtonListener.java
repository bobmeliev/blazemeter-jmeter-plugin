package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/31/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditJMXLocallyButtonListener implements ActionListener {
    /**
     * Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (BmTestManager.getInstance().getTestInfo().getId().isEmpty()) {
            JMeterUtils.reportErrorToUser("JMX can not be downloaded: test id is empty", "Empty test id");
            return;
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage.isDirty()) {
            int chosenOption = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    "Do you want to save changes in current test-plan?",
                    JMeterUtils.getResString("save?"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (chosenOption == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (chosenOption == JOptionPane.YES_OPTION) {

                Utils.saveJMX(guiPackage);


                Utils.downloadJMX();
            } else if (chosenOption == JOptionPane.NO_OPTION) {
                Utils.downloadJMX();
            }
        } else {
            Utils.downloadJMX();
        }

    }
}
