package com.blazemeter.jmeter.utils;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

import java.io.File;

/**
 */
public abstract class JMeterPluginUtils {
    public static String getProjectName() {
        if (GuiPackage.getInstance() == null)
            return null;
        String projectPath = GuiPackage.getInstance().getTestPlanFile();
        String filename = "untitled";
        if (projectPath != null) {
            filename = new File(projectPath).getName();
            if (filename.length() > 4)
                filename = filename.toLowerCase().endsWith(".jmx") ? filename.substring(0, filename.length() - 4) : filename;
        }
        return filename;
    }

    public static boolean inCloudConfig() {
        return JMeterUtils.getPropDefault("blazemeter.is_image", false);
    }

    public static String getJmeterVersion() {
        String version = JMeterUtils.getJMeterVersion();

        int hyphenIndex = version.indexOf("-");
        if (hyphenIndex != -1)
            return version.substring(0, hyphenIndex);
        else
            return version.substring(0, 3);

    }

    public static PluginVersion getPluginVersion() {
        return new PluginVersion(1, 55, "201306080954"); //YY-MM-DD-HH-MM
        //should be changed before building version for publishing
    }
}

