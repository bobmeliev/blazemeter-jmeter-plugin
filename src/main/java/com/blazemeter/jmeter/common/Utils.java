package com.blazemeter.jmeter.common;

import java.awt.*;
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
/*
                                                ################  Uncomment after receiving URL to blazemeterPlugin.jar
    public static void saveUrl(String filename, String urlString) throws MalformedURLException, IOException
            {
                BufferedInputStream in = null;
                FileOutputStream fout = null;
                try
                {
                        in = new BufferedInputStream(new URL(urlString).openStream());
                        fout = new FileOutputStream(filename);

                        byte data[] = new byte[1024];
                        int count;
                        while ((count = in.read(data, 0, 1024)) != -1)
                        {
                                fout.write(data, 0, count);
                        }
                }
                finally
                {
                        if (in != null)
                                in.close();
                        if (fout != null)
                                fout.close();
                }
            }
*/

    /**
     * Should be finished after receving direct link to BlazemeterPlugin.jar

    public static void restartJMeter(){
          final String JMETER_START_SCRIPT="jmeter.bat";
          final String[]command = {"cmd.exe",
                                   "C:\\Program Files\\Apache Software Foundation\\apache-jmeter-2.7\\lib\\ext",
                                   JMETER_START_SCRIPT};

//          command[1]=JMETER_START_SCRIPT;
//            final ProcessBuilder builder = new ProcessBuilder(command);
        try{
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(command);
            //
//            builder.start();
            System.exit(0);

        }catch(IOException e){
            BmLog.error("Unable to restart JMeter after updating plugin",e);
        }

        }


     */

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
/*                                Finish after receiving direct link to BlazemeterPlugin.jar
    public static class PluginInstaller extends MouseAdapter{

        private String PLUGIN_INSTALLER_URI="http://cloud.github.com/downloads/KentBeck/junit/junit-dep-4.11-SNAPSHOT-20120805-1225.jar";;
        private String PLUGIN_ABSOLUTE_PATH="../lib/ext/blazemeter_jmeter_plugin.jar";

        public PluginInstaller() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
               *//*try{                                                 ###### TBD with ALON
                                                          Need direct link to gitHUB BlazmeterPlugin.jar.
                   saveUrl(PLUGIN_ABSOLUTE_PATH,PLUGIN_INSTALLER_URI);
               } catch(MalformedURLException exception){
                       BmLog.error("Wrong URL", exception);
               }
                 catch(IOException exception){
                       BmLog.error("Error while saving file", exception);
               }
                restartJMeter();*//*
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

    }*/
}
