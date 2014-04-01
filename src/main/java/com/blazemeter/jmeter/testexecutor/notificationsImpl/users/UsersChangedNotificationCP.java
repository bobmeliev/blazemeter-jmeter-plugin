package com.blazemeter.jmeter.testexecutor.notificationsImpl.users;

import com.blazemeter.jmeter.entities.Plan;
import com.blazemeter.jmeter.entities.Users;
import com.blazemeter.jmeter.testexecutor.notifications.IUsersChangedNotification;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;
import org.json.JSONArray;

import javax.swing.*;
import java.util.Dictionary;

/**
 * Created by dzmitrykashlach on 3/5/14.
 */
public class UsersChangedNotificationCP implements IUsersChangedNotification {
    private JSlider numberOfUsersSlider;
    private CloudPanel cloudPanel;

    public UsersChangedNotificationCP(JSlider numberOfUsersSlider, CloudPanel cloudPanel) {
        this.numberOfUsersSlider = numberOfUsersSlider;
        this.cloudPanel = cloudPanel;
    }

    @Override
    public void onUsersChanged(Users users) {
        if (users == null) {
            return;
        } else {
            Plan plan = users.getPlan();
            int maxUsers = plan.getConcurrency();
            //configure numberOfUserSlider depending on UserInfo
            numberOfUsersSlider.setMinimum(0);
            numberOfUsersSlider.setMaximum(maxUsers);
            numberOfUsersSlider.setMajorTickSpacing(maxUsers / 4);
            numberOfUsersSlider.setMinorTickSpacing(maxUsers / 12);
            Dictionary labels = numberOfUsersSlider.createStandardLabels(numberOfUsersSlider.getMajorTickSpacing());
            numberOfUsersSlider.setLabelTable(labels);

            //set locations list
            JSONArray locations = users.getLocations();
            cloudPanel.setLocations(locations);
        }
    }
}
//}