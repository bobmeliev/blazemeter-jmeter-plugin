package com.blazemeter.jmeter.utils.mouseadapters;

import com.blazemeter.jmeter.api.BmUrlManager;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.GuiUtils;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by dzmitrykashlach on 1/10/14.
 */
public class PluginInstaller extends MouseAdapter {
    public PluginInstaller() {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {

            try {
                GuiUtils.navigate(BmUrlManager.getPluginPage());
            } catch (Throwable exception) {
                BmLog.error("Wrong URL", exception);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
