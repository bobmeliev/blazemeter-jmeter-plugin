package com.blazemeter.jmeter.testexecutor;

//~--- non-JDK imports --------------------------------------------------------

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.notifications.IPluginUpdateNotification;
import com.blazemeter.jmeter.testexecutor.panels.TestPanelGui;
import com.blazemeter.jmeter.testexecutor.panels.VersionPanel;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Constants;
import com.blazemeter.jmeter.utils.PluginUpdate;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class RemoteTestRunnerGui extends AbstractVisualizer implements ActionListener, IPluginUpdateNotification {
    private static TestPanelGui gui;
    private static JPanel versionPanel;


    public static JPanel getVersionPanel() {
        return versionPanel;
    }


    public RemoteTestRunnerGui() {
        super();
        Utils.checkJMeterVersion();
        try {
            gui = TestPanelGui.getGui();
        } catch (Exception e) {
            BmLog.error("Failed to construct RemoteTestRunnerGui instance:" + e);
        }
        init();
        getFilePanel().setVisible(false);
    }


    public TestElement createTestElement() {
        RemoteTestRunner testRunner = new RemoteTestRunner();
        modifyTestElement(testRunner);
        return testRunner;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);
        BmTestManager bmTestManager = BmTestManager.getInstance();
        RemoteTestRunner remoteTestRunner = (RemoteTestRunner) te;

        TestInfo testInfo = bmTestManager.getTestInfo();
        String userKey = BmTestManager.getInstance().getUserKey();
        remoteTestRunner.setUserKey(userKey);
        remoteTestRunner.setTestInfo(testInfo);
        remoteTestRunner.setReportName("test_" + testInfo.getId() + ".jtl");
    }

    @Override
    public void clearGui() {
        super.clearGui();
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        BmTestManager bmTestManager = BmTestManager.getInstance();
        RemoteTestRunner remoteTestRunner = (RemoteTestRunner) element;
        String userKey = remoteTestRunner.getUserKey();
        TestInfo testInfo = null;

        if (GuiPackage.getInstance().getTestPlanFile() == null) {
            if (userKey.matches(Constants.USERKEY_REGEX)) {
                bmTestManager.setUserKey(userKey);
                testInfo = remoteTestRunner.getTestInfo();
                bmTestManager.setTestInfo(testInfo);
                JMeterUtils.setProperty(Constants.CURRENT_TEST, testInfo.getId() + ";" + testInfo.getName());
            } else if (!userKey.isEmpty()) {
                JMeterUtils.reportErrorToUser("UserKey " + '"' + userKey + '"' + " has invalid format",
                        "Invalid UserKey format");
            }
        }


        boolean isLocalRunMode = remoteTestRunner.getIsLocalRunMode();
        bmTestManager.setIsLocalRunMode(isLocalRunMode);
        gui.init();

        bmTestManager.getInstance().checkForUpdates();
    }

    private Component getTopPanel() {
        Container panel = makeTitlePanel();
        if (!Desktop.isDesktopSupported()) {
            return panel;
        }

        JLabel icon = new JLabel();

        icon.setIcon(
                new ImageIcon(
                        Utils.class.getResource("/com/blazemeter/jmeter/images/BlazemeterLogoB.png")));
        icon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        icon.addMouseListener(new Utils.URIOpener(BlazemeterApi.BmUrlManager.getServerUrl()));

        JLabel version = new JLabel("Version:" + Utils.getPluginVersion().toString());

        version.setFont(version.getFont().deriveFont(Font.PLAIN).deriveFont(14F));
        version.setForeground(Color.GRAY);

        versionPanel = VersionPanel.getVersionPanel();

        Container innerPanel = findComponentWithBorder((JComponent) panel, EtchedBorder.class);
        JPanel panelLink = VersionPanel.getVersionPanel().getPanelLink();
        if (innerPanel != null) {

            innerPanel.add(panelLink);
        } else {
            panel.add(panelLink);
        }
        return panel;
    }

    private void init() {

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(getTopPanel(), BorderLayout.NORTH);
        box.add(gui.getMainPanel(), BorderLayout.NORTH);
        add(box, BorderLayout.NORTH);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!JMeterUtils.getPropDefault(Constants.BLAZEMETER_RUNNERGUI_INITIALIZED, false)) {
                    JMeterUtils.setProperty(Constants.BLAZEMETER_RUNNERGUI_INITIALIZED, "true");

                    BmTestManager bmTestManager = BmTestManager.getInstance();
                    bmTestManager.pluginUpdateNotificationListeners.add(RemoteTestRunnerGui.this);

                }
            }
        }).start();
    }

    public String getStaticLabel() {
        return Constants.BLAZEMETER_LABEL;
    }

    public String getLabelResource() {
        return this.getClass().getCanonicalName();
    }

    private static Container findComponentWithBorder(JComponent panel, Class<?> aClass) {
        for (int n = 0; n < panel.getComponentCount(); n++) {
            if (panel.getComponent(n) instanceof JComponent) {
                JComponent comp = (JComponent) panel.getComponent(n);

                if ((comp.getBorder() != null) && aClass.isAssignableFrom(comp.getBorder().getClass())) {
                    return comp;
                }

                Container con = findComponentWithBorder(comp, aClass);

                if (con != null) {
                    return con;
                }
            }
        }

        return null;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void onPluginUpdate(final PluginUpdate update) {
        if (update == null)
            return;

        versionPanel.removeAll();

        JLabel newVersion = new JLabel(String.format("New version - %s, is available", update.getVersion().toString()));
        newVersion.setForeground(Color.WHITE);
        versionPanel.add(newVersion);
        JLabel moreInfo = new JLabel();
        moreInfo.setText("<html><u>More info</u></html>");
        moreInfo.setToolTipText("Click here to see changes in new version");
        moreInfo.setForeground(Color.WHITE);
        moreInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        moreInfo.addMouseListener(new MouseListener() {
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
                    Utils.Navigate(update.getMoreInfoUrl());
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
        });
        versionPanel.add(moreInfo);
        JLabel download = new JLabel("<html><u>Download</u></html>");
        download.setForeground(Color.WHITE);
        download.setCursor(new Cursor(Cursor.HAND_CURSOR));
        download.setToolTipText("Click here to download new version");
        Utils.PluginInstaller pluginInstaller = new Utils.PluginInstaller();
        download.addMouseListener(pluginInstaller);
        versionPanel.add(download);


    }

    public void add(SampleResult sample) {

    }

    public void clearData() {

    }
}

