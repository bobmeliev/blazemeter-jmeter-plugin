package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.TestInfoController;
import com.blazemeter.jmeter.entities.EnginesParameters;
import com.blazemeter.jmeter.entities.Overrides;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.testexecutor.notificationsImpl.TestListNotification;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;
import com.blazemeter.jmeter.testexecutor.panels.PropertyPanel;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;

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


    public static void navigate(String url) {
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
        ITestListReceivedNotification testListReceivedNotification = new TestListNotification(applyNotificationTo, false);
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

    public static void startInTheCloud(JSlider numberOfUsersSlider,
                                       JSpinner durationSpinner,
                                       JSpinner iterationsSpinner,
                                       JSpinner rampupSpinner) {
        saveCloudTest(numberOfUsersSlider,
                durationSpinner,
                iterationsSpinner,
                rampupSpinner);
        BmTestManager bmTestManager = BmTestManager.getInstance();
        TestInfoController.stop();
        bmTestManager.runInTheCloud();
        TestInfo testInfo = bmTestManager.getTestInfo();
        if (testInfo.getError() == null & testInfo.getStatus() == TestStatus.Running) {
            String url = bmTestManager.getTestUrl();
            if (url != null)
                url = url.substring(0, url.length() - 5);
            GuiUtils.navigate(url);
        }
    }

    /*
    Called before starting test.
    This method updates test settings on BM server
     */
    public static void saveCloudTest(JSlider numberOfUsersSlider,
                                     JSpinner durationSpinner,
                                     JSpinner iterationsSpinner,
                                     JSpinner rampupSpinner) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        int numberOfUsers = numberOfUsersSlider.getValue();
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(numberOfUsers);

        int userPerEngine = enginesParameters.getUserPerEngine();


        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        if (testInfo != null) {
            if (userPerEngine == 0) {
                JMeterUtils.reportErrorToUser("Can't set up test with 0 users. " +
                        " '1' will be saved");
                userPerEngine = 1;
                Overrides overrides = testInfo.getOverrides();
                if (overrides != null) {
                    testInfo.getOverrides().setThreads(userPerEngine);
                } else {
                    overrides = new Overrides((Integer) durationSpinner.getValue(),
                            (Integer) iterationsSpinner.getValue(),
                            (Integer) rampupSpinner.getValue(),
                            userPerEngine);
                    testInfo.setOverrides(overrides);
                }
                testInfo.setNumberOfUsers(Integer.valueOf(userPerEngine));
            }
            /*
            BPC-207
             */
            PropertyPanel propertyPanel = (PropertyPanel) TestPanel.getTestPanel().getAdvancedPropertiesPane().getjMeterPropertyPanel();
            Properties jmeterProperties = propertyPanel.getData();
            testInfo.setJmeterProperties(jmeterProperties);
            /*
            BPC-207
             */
            testInfo = bmTestManager.updateTestSettings(bmTestManager.getUserKey(),
                    bmTestManager.getTestInfo());
            bmTestManager.setTestInfo(testInfo);

        } else {
            JMeterUtils.reportErrorToUser("Please, select test", "Test is not selected");
        }
    }

    public static void runModeChanged(JRadioButton runLocal, JRadioButton runRemote,
                                      CloudPanel cloudPanel, JTabbedPane advancedPropertiesPane,
                                      boolean isLocalRunMode) {
        runLocal.setSelected(isLocalRunMode);
        runRemote.setSelected(!isLocalRunMode);
        cloudPanel.setVisible(!isLocalRunMode);
        advancedPropertiesPane.setVisible(!isLocalRunMode);
    }

    public static void clearTestInfo(JComboBox testIdComboBox) {
        testIdComboBox.removeAllItems();
        BmTestManager.getInstance().setTestInfo(null);
    }
}
