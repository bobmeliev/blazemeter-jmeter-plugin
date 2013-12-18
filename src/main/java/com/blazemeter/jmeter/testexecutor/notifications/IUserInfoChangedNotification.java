package com.blazemeter.jmeter.testexecutor.notifications;

import com.blazemeter.jmeter.entities.UserInfo;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/21/13
 * Time: 11:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IUserInfoChangedNotification {
    void onUserInfoChanged(UserInfo userInfo);
}
