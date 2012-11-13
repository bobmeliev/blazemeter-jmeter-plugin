package com.blazemeter.jmeter.common;

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
        int spaceIndex = version.indexOf(" ");
        if (spaceIndex != -1)
            return version.substring(0, spaceIndex);
        else
            return version;
    }

    public static PluginVersion getPluginVersion() {
        return new PluginVersion(1, 52, "1211131619"); //YY-MM-DD-HH-SS
        //should be changed before building version for GitHub
    }

}

