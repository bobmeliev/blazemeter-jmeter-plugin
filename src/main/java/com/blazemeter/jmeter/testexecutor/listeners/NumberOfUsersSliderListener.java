package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.entities.EnginesParameters;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Created by dzmitrykashlach on 12/24/13.
 */
public class NumberOfUsersSliderListener implements ChangeListener {
    private JTextField numberOfUserTextBox;
    private JSlider numberOfUsersSlider;

    private JTextArea enginesDescription;

    public NumberOfUsersSliderListener(JTextField numberOfUserTextBox,
                                       JSlider numberOfUsersSlider,
                                       JTextArea enginesDescription) {
        this.numberOfUserTextBox = numberOfUserTextBox;
        this.numberOfUsersSlider = numberOfUsersSlider;
        this.enginesDescription = enginesDescription;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int numberOfUsers = numberOfUsersSlider.getValue();
        int consoles;
        int engines;
        StringBuilder engineSize = new StringBuilder();
        int usersPerEngine;
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        testInfo.setNumberOfUsers(numberOfUsers);
        EnginesParameters enginesParameters = EnginesParameters.getEnginesParameters(numberOfUsers);
        consoles = Integer.valueOf(enginesParameters.getConsoles());
        engines = Integer.valueOf(enginesParameters.getEngines());
        engineSize.append(enginesParameters.getEngineSize().equals("m1.medium") ? "MEDIUM ENGINE(S)" : "LARGE ENGINE(S)");
        usersPerEngine = enginesParameters.getUserPerEngine();

        enginesDescription.removeAll();
        enginesDescription.setText(consoles + " CONSOLE(S) x " + usersPerEngine + "users\n"
                + String.format("%d %s x %d users", engines, engineSize, usersPerEngine));
        numberOfUserTextBox.setText(String.valueOf(numberOfUsers));
        engineSize.setLength(0);
    }
}
