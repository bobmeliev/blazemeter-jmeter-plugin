package com.blazemeter.jmeter.testexecutor.listeners;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/18/13
 * Time: 7:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class NumberOfUsersSliderListener //implements ChangeListener
{

    /*@Override
    public void stateChanged(ChangeEvent e) {
        JSlider numberOfUsersSlider = (JSlider)e.getSource();
        int numberOfUsers = numberOfUsersSlider.getValue();
        int engines;
        String engineSize;
        int usersPerEngine;

        ArrayList<String> enginesParameters = calculateEnginesForTest(numberOfUsers);
        engines = Integer.valueOf(enginesParameters.get(0));
        engineSize = enginesParameters.get(1).equals("m1.medium") ? "MEDIUM ENGINE" : "LARGE ENGINE";
        usersPerEngine = Integer.valueOf(enginesParameters.get(2));
        if (numberOfUsers <= 300) {
            enginesDescription.setText(String.format("JMETER CONSOLE -  %d users", usersPerEngine));
            numberOfUserTextBox.setText(Integer.toString(numberOfUsers));
        } else {
            enginesDescription.setText(String.format("%d %s x %d users", engines, engineSize, usersPerEngine));
            numberOfUserTextBox.setText(Integer.toString(usersPerEngine * engines));
        }
    }*/
}
