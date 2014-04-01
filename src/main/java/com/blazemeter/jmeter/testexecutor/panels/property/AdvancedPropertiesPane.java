package com.blazemeter.jmeter.testexecutor.panels.property;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 12/26/13.
 */
public class AdvancedPropertiesPane extends JTabbedPane {
    private JPanel jMeterPropertyPanel = PropertyPanelFactory.getjMeterPropertyPanel();
    private JPanel hostsOverridePanel = PropertyPanelFactory.getHostsOverridePanel();

    AdvancedPropertiesPane() {
        super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        addTab("JMeter Properties", null, jMeterPropertyPanel, "JMeter Properties");
//        addTab("Hosts Override", null, hostsOverridePanel, "Hosts Override");
    }

    public JPanel getjMeterPropertyPanel() {
        return jMeterPropertyPanel;
    }

    public void setjMeterPropertyPanel(JPanel jMeterPropertyPanel) {
        this.jMeterPropertyPanel = jMeterPropertyPanel;
    }

    public JPanel getHostsOverridePanel() {
        return hostsOverridePanel;
    }

    public void setHostsOverridePanel(JPanel hostsOverridePanel) {
        this.hostsOverridePanel = hostsOverridePanel;
    }
}
