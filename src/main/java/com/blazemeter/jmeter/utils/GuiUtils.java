package com.blazemeter.jmeter.utils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by dzmitrykashlach on 12/19/13.
 */
public class GuiUtils {
    private GuiUtils() {
    }


    public static void addTestId(JComboBox testIdComboBox, Object test, boolean selected) {
        testIdComboBox.addItem(test);
        if (selected) {
            testIdComboBox.setSelectedItem(test);
        }
    }


    public static void Navigate(String url) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException e) {
                BmLog.error(e);
            } catch (URISyntaxException e) {
                BmLog.error(e);
            } catch (NullPointerException npe) {
                BmLog.error("URL is empty, nothing to open in browser", npe);
            }
        }
    }
}
