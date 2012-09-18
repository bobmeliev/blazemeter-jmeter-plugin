package com.blazemeter.jmeter.testexecutor;

//~--- non-JDK imports --------------------------------------------------------

import com.blazemeter.jmeter.common.*;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class RemoteTestRunnerGui extends AbstractListenerGui implements ActionListener, BmTestManager.PluginUpdateReceived {
    static TestPanelGui gui;


    public RemoteTestRunnerGui() {
        super();
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("RemoteTestRunnerGui(),Running in the cloud!");
            this.setEnabled(false);
            return;
        }
        try {
            if(gui==null)
            {
                gui = new TestPanelGui();
            }
        } catch (Exception e) {
            BmLog.error(e);
        }
        init();
        BmTestManager.getInstance().pluginUpdateReceivedNotificationListeners.add(this);
    }

    public TestElement createTestElement() {
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("RemoteTestRunnerGui.createTestElement,Running in the cloud!");
            return null;
        }
        BmTestManager.getInstance().checkForUpdates();
        RemoteTestRunner testRunner = new RemoteTestRunner();
        testRunner.setUserKey(BmTestManager.getInstance().getUserKey());
        testRunner.setTestInfo(BmTestManager.getInstance().getTestInfo());
        testRunner.setIsLocalRunMode(BmTestManager.getInstance().getIsLocalRunMode());
        modifyTestElement(testRunner);
        return testRunner;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("RemoteTestRunnerGui.modifyTestElement,Running in the cloud!");
            return;
        }
        super.configureTestElement(te);
        RemoteTestRunner runner = (RemoteTestRunner) te;
        runner.setReportName(gui.getReportName());
    }

    @Override
    public void clearGui() {
        super.clearGui();
    }


    @Override
    public void configure(TestElement element) {
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("RemoteTestRunnerGui.Configure,Running in the cloud!");
            return;
        }
        super.configure(element);

        RemoteTestRunner runner = (RemoteTestRunner) element;
        BmTestManager.getInstance().setUserKey(runner.getUserKey());
        BmTestManager.getInstance().setTestInfo(runner.getTestInfo());
        BmTestManager.getInstance().setIsLocalRunMode(runner.getIsLocalRunMode());

        gui.setReportName(runner.getReportName());
    }
    JPanel versionPanel;
    private Component getTopPanel() {
        Container panel = makeTitlePanel();

        if (!Desktop.isDesktopSupported()) {
            return panel;
        }

        JLabel icon = new JLabel();

        icon.setIcon(
                new ImageIcon(
                        JMeterPluginUtils.class.getResource("/com/blazemeter/jmeter/common/BlazemeterLogoB.png")));
        icon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        icon.addMouseListener(new Utils.URIOpener(BlazemeterApi.BmUrlManager.getServerUrl()));

        JLabel version = new JLabel("Version:" +JMeterPluginUtils.getPluginVersion().toString());

        version.setFont(version.getFont().deriveFont(Font.PLAIN).deriveFont(14F));
        version.setForeground(Color.GRAY);


        versionPanel= new JPanel();
        versionPanel.setBackground(new Color(47, 41, 43));
        versionPanel.setForeground(Color.GRAY);
        Container innerPanel = findComponentWithBorder((JComponent) panel, EtchedBorder.class);
        JPanel panelLink = new JPanel(new GridBagLayout());
        panelLink.setBackground(new Color(47, 41, 43));

        JLabel reportBug = new JLabel();
        reportBug.setText("<html><u>Report a bug</u></html>");
        reportBug.setToolTipText("Click here to report a bug");
        reportBug.setForeground(Color.WHITE);
        reportBug.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel requestFeature = new JLabel();
        requestFeature.setText("<html><u>Request a feature</u></html>");
        requestFeature.setToolTipText("Click here to request a feature");
        requestFeature.setForeground(Color.WHITE);
        requestFeature.setCursor(new Cursor(Cursor.HAND_CURSOR));


        GridBagConstraints gridBagConstraints;

        gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 1, 0, 0);
        gridBagConstraints.gridheight=2;
        panelLink.add(icon, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(2, 0, 0, 10);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        panelLink.add(reportBug, gridBagConstraints);
        reportBug.addMouseListener(new Utils.URIOpener(Utils.REQUEST_FEATURE_REPORT_BUG_URL));


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(2, 0, 0, 10);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        panelLink.add(requestFeature, gridBagConstraints);
        requestFeature.addMouseListener(new Utils.URIOpener(Utils.REQUEST_FEATURE_REPORT_BUG_URL));



        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(2, 0, 0, 4);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        panelLink.add(version, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);

        panelLink.add(versionPanel, gridBagConstraints);





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
        box.add(gui.mainPanel, BorderLayout.NORTH);
        add(box, BorderLayout.NORTH);
    }

    public String getStaticLabel() {
        return "BlazeMeter";
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
    public void onPluginUpdateReceived(final PluginUpdate update) {
        if(update==null)
            return;

        versionPanel.removeAll();

        JLabel newVersion = new JLabel(String.format("New version - %s, is available",update.getVersion().toString()));
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
                if(JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(null,
                        "Main changes are:\n" +
                                update.getChanges()+
                                "\n\nFull list of changes can be viewed on our site,\nDo you want to see full list of changes?",
                        "Changes list",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,null,null)){
                    Utils.Navigate(update.getMoreInfoUrl());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
        versionPanel.add(moreInfo);
        JLabel download = new JLabel("<html><u>Download&Install</u></html>");
        download.setForeground(Color.WHITE);
        download.setCursor(new Cursor(Cursor.HAND_CURSOR));
        download.setToolTipText("Click here to download and install new version");
        // should be changed to new Utils.PluginInstaller after Alon will provide direct URL to BlazemeterPlugin.jar
        download.addMouseListener(new Utils.URIOpener(update.getDownloadUrl()));
        versionPanel.add(download);
    }
}

