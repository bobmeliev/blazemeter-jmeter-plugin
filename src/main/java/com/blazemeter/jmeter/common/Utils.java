package com.blazemeter.jmeter.common;

import com.blazemeter.jmeter.testexecutor.RemoteTestRunnerGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 */
public class Utils {

    public static String REQUEST_FEATURE_REPORT_BUG_URL = "http://community.blazemeter.com/forums/175399-blazemeter-jmeter-plugin";

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;

    }

    public static String getFileContents(String fn) {
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);
        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line;
                String newline = System.getProperty("line.separator");
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

    public static void Navigate(String url) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException e) {
                BmLog.error(e);
            } catch (URISyntaxException e) {
                BmLog.error(e);
            }
        }
    }

    public static boolean saveUrl(String filename, String urlString) throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } catch (MalformedURLException e) {
            BmLog.error("Invalid updating URL!");
            return false;
        } catch (IOException e) {
            BmLog.error("Unable to download and save file!");
            return false;
        } finally {
            if (in != null)
                in.close();
            if (fout != null)
                fout.close();
        }
        return true;
    }

    /*
      This method closes JMeter and restarts it using daemon-thread.
    */
    public static void restartJMeter() {
        final String CMD = "cmd.exe";
        final String JMETER_START_SCRIPT = " C:\\Program Files\\Apache Software Foundation\\apache-jmeter-2.8\\bin\\jmeter.bat";
        final String[] command = {CMD,"/C",JMETER_START_SCRIPT};


        try {
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                BmLog.console(line);
            }
            System.exit(0);
        } catch (IOException e) {
            BmLog.error("jmeter.bat is not found - JMeter is not restarted");
        }

    }


    public static class URIOpener extends MouseAdapter {
        private final String uri;

        public URIOpener(String aURI) {
            uri = aURI;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                Navigate(uri);
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

    public static class PluginInstaller extends MouseAdapter {

        public PluginInstaller() {
        }


        private String PLUGIN_UPDATE_URI = "http://cloud.github.com/downloads/Blazemeter/blazemeter-jmeter-plugin/BlazemeterPlugin.jar";
        private String PLUGIN_LOCAL_PATH = "../lib/ext/blazemeter_jmeter_plugin.jar";
        public static boolean isPluginDownloaded = false;
        public static JPanel versionPanel = RemoteTestRunnerGui.getVersionPanel();

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {

                try {

                    isPluginDownloaded = saveUrl(PLUGIN_LOCAL_PATH, PLUGIN_UPDATE_URI);
                    JOptionPane.showMessageDialog(versionPanel, "Please, restart JMeter manually to \n apply changes",
                            "Manual restart is needed",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (MalformedURLException exception) {
                    BmLog.error("Wrong URL", exception);
                } catch (IOException exception) {
                    BmLog.error("Error while saving file", exception);
                }
//                restartJMeter();
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
