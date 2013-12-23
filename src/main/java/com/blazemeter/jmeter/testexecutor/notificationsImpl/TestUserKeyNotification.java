package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.TestInfoController;
import com.blazemeter.jmeter.controllers.TestListController;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestListReceivedNotification;
import com.blazemeter.jmeter.testexecutor.notifications.ITestUserKeyNotification;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by dzmitrykashlach on 12/20/13.
 */
public class TestUserKeyNotification implements ITestUserKeyNotification {
    JButton signUpButton;
    JComboBox testIdComboBox;
    JPanel mainPanel;
    CloudPanel cloudPanel;


    public TestUserKeyNotification(JButton signUpButton,
                                   JComboBox testIdComboBox,
                                   JPanel mainPanel,
                                   CloudPanel cloudPanel) {
        this.signUpButton = signUpButton;
        this.testIdComboBox = testIdComboBox;
        this.mainPanel = mainPanel;
        this.cloudPanel = cloudPanel;


    }

    @Override
    public void onTestUserKeyChanged(String userKey) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        boolean isUserKeyValid = bmTestManager.isUserKeyValid();
        if (signUpButton.isVisible()) {
            signUpButton.setEnabled(!(userKey.matches(Constants.USERKEY_REGEX) & isUserKeyValid));
        }
        if (isUserKeyValid) {
            HashMap<String, Object> applyNotificationTo = new HashMap<String, Object>();
            applyNotificationTo.put(TestListNotification.TEST_ID_COMBOBOX, testIdComboBox);
            applyNotificationTo.put(TestListNotification.MAIN_PANEL, mainPanel);
            applyNotificationTo.put(TestListNotification.CLOUD_PANEL, cloudPanel);
            ITestListReceivedNotification testListNotification = new TestListNotification(applyNotificationTo);
            TestListController.start(userKey, testListNotification);
        } else {
            TestListController.stop();
            TestInfoController.stop();
            bmTestManager.setUserKeyValid(false);

        }
    }
}
