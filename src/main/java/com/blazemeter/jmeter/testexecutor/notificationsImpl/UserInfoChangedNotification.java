package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.entities.UserInfo;
import com.blazemeter.jmeter.testexecutor.notifications.IUserInfoChangedNotification;
import com.blazemeter.jmeter.utils.GuiUtils;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 12/26/13.
 */
public class UserInfoChangedNotification implements IUserInfoChangedNotification {
    private JLabel userInfoLabel;
    private JComboBox testIdComboBox;

    public UserInfoChangedNotification(JLabel userInfoLabel, JComboBox testIdComboBox) {
        this.userInfoLabel = userInfoLabel;
        this.testIdComboBox = testIdComboBox;
    }

    @Override
    public void onUserInfoChanged(UserInfo userInfo) {
        if (userInfo == null) {
            userInfoLabel.setText("");
            GuiUtils.clearTestInfo(testIdComboBox);
        } else {
            if (userInfo.getMaxUsersLimit() > 8400 && userInfo.getMaxEnginesLimit() > 14) {
                userInfo.setMaxUsersLimit(8400);
                userInfo.setMaxEnginesLimit(14);
            }
            userInfoLabel.setText(userInfo.toString());

        }

    }
}
