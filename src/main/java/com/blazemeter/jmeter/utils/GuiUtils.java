package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.testexecutor.notificationsImpl.TestListNotification;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

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


    public static void Navigate(String url) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException e) {
                BmLog.error(e);
            } catch (URISyntaxException e) {
                BmLog.error(e);
            } catch (NullPointerException npe) {
                BmLog.error("URL is empty, nothing to open in browser", npe);
            }
        }
    }

    public static void getUserTests(JComboBox testIdComboBox, JPanel mainPanel, CloudPanel cloudPanel, String userKey) {
        if (userKey == null || userKey.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Please enter user key", "No user key", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.setUserKey(userKey);
        testIdComboBox.removeAllItems();
        testIdComboBox.addItem("LOADING...");
        testIdComboBox.setEnabled(false);
        HashMap<String, Object> applyNotificationTo = new HashMap<String, Object>();
        applyNotificationTo.put(TestListNotification.TEST_ID_COMBOBOX, testIdComboBox);
        applyNotificationTo.put(TestListNotification.MAIN_PANEL, mainPanel);
        applyNotificationTo.put(TestListNotification.CLOUD_PANEL, cloudPanel);
        ITestListReceivedNotification testListReceivedNotification = new TestListNotification(applyNotificationTo);
        BmTestManager.getInstance().getTestsAsync(userKey, testListReceivedNotification);
    }


    public static boolean validUserKeyField(JTextField userKeyTextField) {
        boolean valid = false;
        String userKey = userKeyTextField.getText();
        if (userKey.matches(Constants.USERKEY_REGEX)) {
            Border greyBorder = BorderFactory.createLineBorder(Color.GRAY);
            userKeyTextField.setBorder(greyBorder);
            valid = true;
        } else {
            Border redBorder = BorderFactory.createLineBorder(Color.RED);
            userKeyTextField.setBorder(redBorder);
        }
        return valid;
    }
}
