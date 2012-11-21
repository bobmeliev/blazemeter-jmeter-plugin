package com.blazemeter.jmeter.testinfo;

import com.blazemeter.jmeter.testexecutor.Overrides;
import com.blazemeter.jmeter.utils.TestStatus;
import com.blazemeter.jmeter.utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/28/12
 * Time: 15:43
 */
public class TestInfo {
    public String id;
    public String name;
    public TestStatus status = null;
    public String error = null;
    public int numberOfUsers;
    public String location;
    public Overrides overrides;
    public String type;


    private TestInfo(String testName, String testId) {
        this.name = testName;
        this.id = testId;
    }

    public TestInfo() {
        this("", "");
    }

    @Override
    public String toString() {

        String ret = String.format("%s - %s", id, name);
        if (error != null)
            ret += "error: " + error;
        return ret;
    }

    public boolean equals(TestInfo ti) {
        return ti != null &&
                ti.name.equals(ti.name) &&
                ti.id.equals(this.id);
    }

    public boolean isEmpty() {
        return (id == null) && (name == null);
    }

    public boolean isValid() {
        return Utils.isInteger(id) && error == null;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public String getLocation() {
        return location;
    }
}
