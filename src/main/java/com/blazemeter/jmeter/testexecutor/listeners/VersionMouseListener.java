package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.entities.PluginUpdate;
import com.blazemeter.jmeter.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by dzmitrykashlach on 3/4/14.
 */
public class VersionMouseListener implements MouseListener {
    private PluginUpdate update;

    public VersionMouseListener(PluginUpdate update) {
        this.update = update;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(null,
                "Main changes are:\n" +
                        update.getChanges() +
                        "\n\nFull list of changes can be viewed on our site,\nDo you want to see full list of changes?",
                "Changes list",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, null, null)) {
            GuiUtils.navigate(update.getMoreInfoUrl());
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
