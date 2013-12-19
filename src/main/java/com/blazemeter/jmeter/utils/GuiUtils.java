package com.blazemeter.jmeter.utils;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
public class GuiUtils {
    private GuiUtils() {
    }


    public static void addTestId(JComboBox testIdComboBox, Object test, boolean selected) {
        testIdComboBox.addItem(test);
        if (selected) {
            testIdComboBox.setSelectedItem(test);
        }
    }
}
