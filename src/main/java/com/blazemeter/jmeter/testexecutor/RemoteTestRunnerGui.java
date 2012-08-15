package com.blazemeter.jmeter.testexecutor;

//~--- non-JDK imports --------------------------------------------------------

import com.blazemeter.jmeter.common.BlazemeterApi;
import com.blazemeter.jmeter.common.BmLog;
import com.blazemeter.jmeter.common.JMeterPluginUtils;
import com.blazemeter.jmeter.common.Utils;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class RemoteTestRunnerGui extends AbstractListenerGui implements ActionListener {
    TestPanelGui gui;

    private static final String PLUGINS_VERSION = "1.01 (beta)";

    public RemoteTestRunnerGui() {
        super();
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("RemoteTestRunnerGui(),Running in the cloud!");
            this.setEnabled(false);
            return;
        }
        try {
            gui = new TestPanelGui();
        } catch (Exception e) {
            BmLog.error(e);
        }
        init();
    }

    public TestElement createTestElement() {
        if (JMeterPluginUtils.inCloudConfig()) {
            BmLog.console("RemoteTestRunnerGui.createTestElement,Running in the cloud!");
            return null;
        }

        RemoteTestRunner testRunner = new RemoteTestRunner();
        testRunner.setUserKey(BmTestManager.getInstance().getUserKey());
        testRunner.setTestInfo(BmTestManager.getInstance().getTestInfo());
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
//        runner.setUserKey(BmTestManager.getInstance().getUserKey());
//        runner.setTestInfo(BmTestManager.getInstance().getTestInfo());
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
        gui.setReportName(runner.getReportName());
    }

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

        JLabel version = new JLabel("v" + PLUGINS_VERSION);

        version.setFont(version.getFont().deriveFont(Font.PLAIN).deriveFont(11F));
        version.setForeground(Color.GRAY);

        Container innerPanel = findComponentWithBorder((JComponent) panel, EtchedBorder.class);
        JPanel panelLink = new JPanel(new GridBagLayout());
        panelLink.setBackground(new Color(47, 41, 43));

        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 1, 0, 0);
        panelLink.add(icon, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 2, 3, 0);
        panelLink.add(Box.createHorizontalGlue(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        panelLink.add(version, gridBagConstraints);

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
}

