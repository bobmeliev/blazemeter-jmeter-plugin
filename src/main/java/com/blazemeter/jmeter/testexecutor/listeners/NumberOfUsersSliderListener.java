package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.Utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashMap;

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
        String engineSize;
        int usersPerEngine;
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        testInfo.setNumberOfUsers(numberOfUsers);
        HashMap<String, String> enginesParameters = Utils.countEngines(numberOfUsers);
        consoles = Integer.valueOf(enginesParameters.get(Constants.CONSOLES));
        engines = Integer.valueOf(enginesParameters.get(Constants.ENGINES));
        engineSize = enginesParameters.get(Constants.ENGINE_SIZE).equals("m1.medium") ? "MEDIUM ENGINE(S)" : "LARGE ENGINE(S)";
        usersPerEngine = Integer.valueOf(enginesParameters.get(Constants.USERS_PER_ENGINE));

        enginesDescription.setText(consoles + " CONSOLE(S) x " + usersPerEngine + "users\n"
                + String.format("%d %s x %d users", engines, engineSize, usersPerEngine));
        numberOfUserTextBox.setText(String.valueOf(numberOfUsers));
    }
}
