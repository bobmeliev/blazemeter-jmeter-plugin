package com.blazemeter.jmeter.models;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 3/7/14.
 */
public class LocationModel extends DefaultComboBoxModel {
    public LocationModel() {
        super();
        addElement("EU West (Ireland)");
        addElement("US East (Virginia)");
        addElement("US West (N.California)");
        addElement("US West (Oregon)");
        addElement("Asia Pacific (Singapore)");
        addElement("Japan (Tokyo)");
        addElement("South America (San Paulo)");
        addElement("Australia (Sydney)");

    }
}
