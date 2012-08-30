package com.blazemeter.jmeter.common;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 8/22/12
 * Time: 14:28
 */
public class PluginVersion {
    int major;
    int minor;
    String build;

    //test
    public PluginVersion(int major, int minor, String build) {
        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public String toString() {
        return toString(false);
    }

    public boolean isNewerThan(PluginVersion other) {
        if (major == other.major) {
            return minor > other.minor;
        }
        return major < other.major;
    }

    public String toString(boolean full) {
        if (full)
            return String.format("%d.%2d-%s", major, minor, build);
        return String.format("%d.%2d", major, minor);
    }
}
