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
    public int maxEnginesLimit;
    public int maxThreadsMedium;
    public int maxThreadsLarge;
    public String plan;

    public long time;

    public UserInfo(String userName, int credits, String email, int maxUsersLimit, int maxEnginesLimit, int maxThreadsMedium, int maxThreadsLarge, String plan) {
        time = new Date().getTime();
        this.userName = userName;
        this.credits = credits;
        this.email = email;
        this.maxUsersLimit = maxUsersLimit;
        this.maxEnginesLimit = maxEnginesLimit;
        this.maxThreadsMedium = maxThreadsMedium;
        this.maxThreadsLarge = maxThreadsLarge;
        this.plan = plan;
    }

    public String toString() {
        return String.format("%s (%d)", userName, credits);
    }
}
