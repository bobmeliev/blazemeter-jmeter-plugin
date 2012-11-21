package com.blazemeter.jmeter.testinfo;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 8/29/12
 * Time: 14:25
 */
public class UserInfo {
    public String userName;
    public String email;
    public int credits;
    public int maxUsersLimit;
    public long time;

    public UserInfo(String userName, String email, int credits, int maxUsersLimit) {
        time = new Date().getTime();
        this.userName = userName;
        this.email = email;
        this.credits = credits;
        this.maxUsersLimit = maxUsersLimit;
    }

    public String toString() {
        return String.format("%s (%d)", userName, credits);
    }
}
