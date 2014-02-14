package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.utils.GuiUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dzmitrykashlach on 2/14/14.
 */
public class HelpButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        GuiUtils.navigate(BlazemeterApi.BmUrlManager.getPluginPage());
    }
}
