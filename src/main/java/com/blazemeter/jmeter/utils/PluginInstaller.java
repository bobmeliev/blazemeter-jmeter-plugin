package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.testexecutor.RemoteTestRunnerGui;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by dzmitrykashlach on 1/10/14.
 */
public class PluginInstaller extends MouseAdapter {
    public PluginInstaller() {
    }


    private String PLUGIN_UPDATE_URI = "https://a.blazemeter.com/jmeter-plugin-download";
    private String PLUGIN_LOCAL_PATH = "../lib/ext/blazemeter.jar";
    public static boolean isPluginDownloaded = false;
    public static JPanel versionPanel = RemoteTestRunnerGui.getVersionPanel();

    @Override
    public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {

            try {

                isPluginDownloaded = Utils.downloadFile(PLUGIN_LOCAL_PATH, PLUGIN_UPDATE_URI);
                JOptionPane.showMessageDialog(versionPanel, "Please, restart JMeter manually to \n apply changes",
                        "Manual restart is needed",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (MalformedURLException exception) {
                BmLog.error("Wrong URL", exception);
            } catch (IOException exception) {
                BmLog.error("Error while saving file", exception);
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
