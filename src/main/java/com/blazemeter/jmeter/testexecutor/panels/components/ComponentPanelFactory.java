package com.blazemeter.jmeter.testexecutor.panels.components;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class ComponentPanelFactory {
    private static VersionPanel versionPanel;
    private static CloudPanel cloudPanel;

    public static VersionPanel getVersionPanel() {
        if (versionPanel == null) {
            versionPanel = new VersionPanel();
        }
        return versionPanel;
    }

    public static CloudPanel getCloudPanel() {
        if (cloudPanel == null) {
            cloudPanel = new CloudPanel();
        }
        return cloudPanel;
    }


}
