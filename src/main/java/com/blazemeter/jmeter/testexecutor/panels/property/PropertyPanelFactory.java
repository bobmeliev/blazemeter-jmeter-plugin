package com.blazemeter.jmeter.testexecutor.panels.property;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class PropertyPanelFactory {
    private static HostsOverridePanel hostsOverridePanel;
    private static JMeterPropertyPanel jMeterPropertyPanel;
    private static AdvancedPropertiesPane advancedPropertiesPane;


    public static HostsOverridePanel getHostsOverridePanel() {
        if (hostsOverridePanel == null) {
            hostsOverridePanel = new HostsOverridePanel();
        }
        return hostsOverridePanel;
    }

    public static JMeterPropertyPanel getjMeterPropertyPanel() {
        if (jMeterPropertyPanel == null) {
            jMeterPropertyPanel = new JMeterPropertyPanel();
        }
        return jMeterPropertyPanel;
    }

    public static AdvancedPropertiesPane getAdvancedPropertiesPane() {
        if (advancedPropertiesPane == null) {
            advancedPropertiesPane = new AdvancedPropertiesPane();
        }
        return advancedPropertiesPane;
    }
}
