package com.blazemeter.jmeter.testexecutor;

//~--- non-JDK imports --------------------------------------------------------

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import com.blazemeter.jmeter.testexecutor.panels.VersionPanel;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.URIOpener;
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


public class RemoteTestRunnerGui extends AbstractVisualizer implements ActionListener {
    private static TestPanel testPanel;
    private static JPanel versionPanel;


    public static JPanel getVersionPanel() {
        return versionPanel;
    }


    public RemoteTestRunnerGui() {
        super();
        Utils.checkJMeterVersion();
        try {
            testPanel = TestPanel.getTestPanel();
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
        testPanel.init();

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
        icon.addMouseListener(new URIOpener(BlazemeterApi.BmUrlManager.getServerUrl()));

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
        box.add(testPanel.getMainPanel(), BorderLayout.NORTH);
        add(box, BorderLayout.NORTH);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!JMeterUtils.getPropDefault(Constants.BLAZEMETER_RUNNERGUI_INITIALIZED, false)) {
                    JMeterUtils.setProperty(Constants.BLAZEMETER_RUNNERGUI_INITIALIZED, "true");
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

    public void add(SampleResult sample) {

    }

    public void clearData() {

    }
}

