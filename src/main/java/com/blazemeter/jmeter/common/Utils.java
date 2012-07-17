package com.blazemeter.jmeter.common;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 */
public class Utils {
    public static boolean isInteger(String str){
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;

    }

    public static String getFileContents(String fn) {
        //String output = new Scanner(new File("file.txt")).useDelimiter("\\Z").next();
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);
        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line;
                String newline=System.getProperty("line.separator");
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(newline);
                }
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
        }
        return contents.toString();
    }

    public static class URIOpener extends MouseAdapter {
        private final String uri;

        public URIOpener(String aURI) {
            uri = aURI;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                openInBrowser(uri);
            }
        }

        private static void openInBrowser(String string) {
            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    java.awt.Desktop.getDesktop().browse(new URI(string));
                } catch (IOException ignored) {
                } catch (URISyntaxException ignored) {
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
}
