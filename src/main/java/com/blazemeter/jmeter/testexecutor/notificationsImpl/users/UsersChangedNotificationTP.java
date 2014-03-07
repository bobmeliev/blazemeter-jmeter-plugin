package com.blazemeter.jmeter.testexecutor.notificationsImpl.users;

import com.blazemeter.jmeter.entities.Users;
import com.blazemeter.jmeter.testexecutor.notifications.IUsersChangedNotification;
import com.blazemeter.jmeter.utils.GuiUtils;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 3/5/14.
 */
public class UsersChangedNotificationTP implements IUsersChangedNotification {
    private JLabel userInfoLabel;
    private JComboBox testIdComboBox;

    public UsersChangedNotificationTP(JLabel userInfoLabel, JComboBox testIdComboBox) {
        this.userInfoLabel = userInfoLabel;
        this.testIdComboBox = testIdComboBox;
    }

    @Override
    public void onUsersChanged(Users users) {
        if (users == null) {
            userInfoLabel.setText("");
            GuiUtils.clearTestInfo(testIdComboBox);
            return;
        }

        userInfoLabel.setText(users.toString());

    }


}