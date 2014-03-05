package com.blazemeter.jmeter.testexecutor.notifications;

import com.blazemeter.jmeter.entities.Users;

/**
 * Created by dzmitrykashlach on 3/5/14.
 */
public interface IUsersChangedNotification {
    void onUsersChanged(Users users);
}
