package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.testexecutor.RemoteTestRunner;
import com.blazemeter.jmeter.testexecutor.RemoteTestRunnerGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 */
public class Utils {

    public static String REQUEST_FEATURE_REPORT_BUG_URL = "http://community.blazemeter.com/forums/175399-blazemeter-jmeter-plugin";

    private Utils() {
    }

    ;

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;

    }

    public static String getHostIP() {
        String hostIP = "";
        try {
            hostIP = InetAddress.getLocalHost().getHostAddress();

        } catch (UnknownHostException uhe) {

        }
        return hostIP;
    }

    /*
      This method perform verification whether or not test plan contains
      ThreadGroups. If test-plan does not contain any,
      then "true" is returned, otherwise - "false";

     */
    public static boolean isTestPlanEmpty() {
        JMeterTreeModel jMeterTreeModel = GuiPackage.getInstance().getTreeModel();
        List<JMeterTreeNode> jMeterTreeNodes = jMeterTreeModel.getNodesOfType(AbstractThreadGroup.class);
        return jMeterTreeNodes.size() == 0 ? true : false;
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
            } catch (NullPointerException npe) {
                BmLog.error("URL is empty, nothing to open in browser", npe);
            }
        }
    }

    public static boolean isWindows() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("mac") >= 0);
    }


    public static boolean isUnix() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static void openJMX(File file) {

        FileInputStream reader = null;
        try {

            BmLog.debug("Loading file: " + file);
            reader = new FileInputStream(file);
            HashTree tree = SaveService.loadTree(reader);
            GuiPackage guiPackage = GuiPackage.getInstance();
            guiPackage.setTestPlanFile(file.getAbsolutePath());
            Load.insertLoadedTree(1, tree);

            JMeterTreeModel model = guiPackage.getTreeModel();
            JMeterTreeNode node = (JMeterTreeNode) guiPackage.getTreeModel().getRoot();
            JMeterTreeNode testPlanNode = model.getNodesOfType(TestPlan.class).get(0);
            List<JMeterTreeNode> nodes = Collections.list(testPlanNode.children());
            Iterator<JMeterTreeNode> nodeIterator = nodes.iterator();
            boolean containsRemoteTestRunner = false;
            while (nodeIterator.hasNext()) {
                JMeterTreeNode nextNode = nodeIterator.next();
                if (nextNode.getUserObject() instanceof RemoteTestRunner) {
                    containsRemoteTestRunner = true;
                }
            }

            if (!containsRemoteTestRunner) {
                TestElement remoteTestRunner = guiPackage.createTestElement(RemoteTestRunnerGui.class, RemoteTestRunner.class);
                model.addComponent(remoteTestRunner, testPlanNode);
            }

        } catch (FileNotFoundException fnfe) {
            BmLog.error("JMX file " + file.getName() + "was not found ", fnfe);
        } catch (IllegalUserActionException iuae) {
            BmLog.error(iuae);
        } catch (Exception exc) {
            BmLog.error(exc);
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
        final String[] command = {CMD, "/C", JMETER_START_SCRIPT};


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


        private String PLUGIN_UPDATE_URI = "https://a.blazemeter.com/jmeter-plugin-download";
        //"http://cloud.github.com/downloads/Blazemeter/blazemeter-jmeter-plugin/blazemeter.jar";
        private String PLUGIN_LOCAL_PATH = "../lib/ext/blazemeter.jar";
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
