package com.blazemeter.jmeter.testinfo;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 8/29/12
 * Time: 14:25
 */
public class UserInfo {
    private String userName;
    private String email;
    private int credits;
    private int maxUsersLimit;
    private int maxEnginesLimit;
    private int maxThreadsMedium;
    private int maxThreadsLarge;
    private String plan;
    private long time;

    public UserInfo(String userName, int credits, String email, int maxUsersLimit, int maxEnginesLimit, int maxThreadsMedium, int maxThreadsLarge, String plan) {
        this.time = new Date().getTime();
        this.userName = userName;
        this.credits = credits;
        this.email = email;
        this.maxUsersLimit = maxUsersLimit;
        this.maxEnginesLimit = maxEnginesLimit;
        this.maxThreadsMedium = maxThreadsMedium;
        this.maxThreadsLarge = maxThreadsLarge;
        this.plan = plan;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getMaxUsersLimit() {
        return maxUsersLimit;
    }

    public void setMaxUsersLimit(int maxUsersLimit) {
        this.maxUsersLimit = maxUsersLimit;
    }

    public int getMaxEnginesLimit() {
        return maxEnginesLimit;
    }

    public void setMaxEnginesLimit(int maxEnginesLimit) {
        this.maxEnginesLimit = maxEnginesLimit;
    }

    public int getMaxThreadsMedium() {
        return maxThreadsMedium;
    }

    public void setMaxThreadsMedium(int maxThreadsMedium) {
        this.maxThreadsMedium = maxThreadsMedium;
    }

    public int getMaxThreadsLarge() {
        return maxThreadsLarge;
    }

    public void setMaxThreadsLarge(int maxThreadsLarge) {
        this.maxThreadsLarge = maxThreadsLarge;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String toString() {
        return String.format("%s (%d)", userName, credits);
    }
}
