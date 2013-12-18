package com.blazemeter.jmeter.testexecutor.panels;

import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.controllers.testinfocontroller.TestInfoController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.entities.UserInfo;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.*;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Constants;
import com.blazemeter.jmeter.utils.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/29/12
 * Time: 12:29
 */
public class TestPanel {
    private static TestPanel gui;

    //Gui controls
    private JTextField userKeyTextField;
    private JComboBox testIdComboBox;
    private JPanel mainPanel;
    private JTextArea testIdArea;
    private JTextArea testNameArea;
    private JButton reloadButton;
    private JButton signUpToBlazemeterButton;
    private JButton createNewButton;
    private JButton goToTestPageButton;
    private JButton helpButton;
    private CloudPanel cloudPanel;
    private JRadioButton runRemote;
    private JRadioButton runLocal;
    private JLabel userInfoLabel;
    private JPanel jMeterPropertyPanel = new JMeterPropertyPanel();


    public TestPanel() {
        try {
            createMainPanel();
            reloadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    fetchUserTestsAsync();
                }
            });
            signUpToBlazemeterButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    Utils.Navigate(BmTestManager.getServerUrl() + "/user");
                }
            });

            createNewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    BmTestManager bmTestManager = BmTestManager.getInstance();
                    String userKey = bmTestManager.getUserKey();
                    if (userKey == null || userKey.isEmpty()) {
                        JMeterUtils.reportErrorToUser("Please enter user key", "No user key");
                        bmTestManager.setUserKeyValid(false);
                        return;

                    }
                    String testName = testNameArea.getText().trim();
                    if (testName.isEmpty() | testName.equals(Constants.NEW)) {
                        testName = JOptionPane.showInputDialog(mainPanel, "Please enter valid test name!");
                        if (testName == null || testName.trim().isEmpty())
                            return;
                    }
                    if (Utils.isTestPlanEmpty()) {
                        JMeterUtils.reportErrorToUser("Test-plan should contain at least one Thread Group");
                        return;
                    }
                    int numberOfUsers = cloudPanel.getNumberOfUsers();
                    TestInfo ti = bmTestManager.createTest(userKey, testName);
                    ti.setLocation(cloudPanel.getServerLocation());
                    ti.setNumberOfUsers(50);
                    ti.setStatus(TestStatus.NotRunning);
                    Properties jmeterProperties = null;

                    if (!bmTestManager.getIsLocalRunMode()) {
                        jmeterProperties = bmTestManager.getTestInfo().getJmeterProperties();

                    } else {
                        jmeterProperties = new Properties();
                    }

                    ti.setJmeterProperties(jmeterProperties);
                    ti = bmTestManager.updateTestSettings(userKey, ti);
                    if (ti != null && ti.getStatus() != null) {
                        addTestId(ti, true);
                        bmTestManager.setTestInfo(ti);
                    }
                    GuiPackage guiPackage = GuiPackage.getInstance();
                    if (guiPackage.getTestPlanFile() == null) {

                        Utils.saveJMX(guiPackage);
                    }
                    bmTestManager.uploadJmx();

                }
            });


            goToTestPageButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String url = BmTestManager.getInstance().getTestUrl();
                    if (url != null) {
                        Utils.Navigate(url);
                    } else {
                        JMeterUtils.reportErrorToUser("Test is not selected. Nothing to open.");
                    }
                }
            });
            helpButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    Utils.Navigate(Constants.HELP_URL);
                }
            });
        } catch (NullPointerException npe) {
            BmLog.error("Failed to construct TestPanel instance: " + npe);
        }

    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    private void enableMainPanelControls(boolean isEnabled) {
        testIdComboBox.setEnabled(isEnabled);
        reloadButton.setEnabled(isEnabled);
        createNewButton.setEnabled(isEnabled);
        goToTestPageButton.setEnabled(isEnabled);
    }


    private void clearTestInfo() {
        testIdComboBox.removeAllItems();
        BmTestManager.getInstance().setTestInfo(null);
    }

    /**
     * Here some heavy GUI listeners are initialized;
     */
    public void init() {
        BmTestManager bmTestManager = BmTestManager.getInstance();

        if (!JMeterUtils.getPropDefault(Constants.BLAZEMETER_TESTPANELGUI_INITIALIZED, false)) {
            JMeterUtils.setProperty(Constants.BLAZEMETER_TESTPANELGUI_INITIALIZED, "true");

            final IRunModeChangedNotification runModeChanged = new IRunModeChangedNotification() {
                @Override
                public void onRunModeChanged(boolean isLocalRunMode) {
                    runModeChanged(isLocalRunMode);

                }
            };
            BmTestManager.getInstance().runModeChangedNotificationListeners.add(runModeChanged);

            ActionListener listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BmTestManager bmTestManager = BmTestManager.getInstance();
                    bmTestManager.setIsLocalRunMode(e.getActionCommand().equals("Locally (Reporting Only)"));
                    boolean isLocalRunMode = bmTestManager.getIsLocalRunMode();
                    runModeChanged.onRunModeChanged(isLocalRunMode);
                    BmLog.console(e.getActionCommand());
                }
            };

            runLocal.addActionListener(listener);
            runRemote.addActionListener(listener);


            signUpToBlazemeterButton.setEnabled(BmTestManager.getInstance().getUserKey() == null || BmTestManager.
                    getInstance().
                    getUserKey().
                    isEmpty());

            BmTestManager.getInstance().userInfoChangedNotificationListeners.add(new IUserInfoChangedNotification() {
                @Override
                public void onUserInfoChanged(UserInfo userInfo) {
                    if (userInfo == null) {
                        userInfoLabel.setText("");
                        clearTestInfo();
                    } else {
                        if (userInfo.getMaxUsersLimit() > 8400 && userInfo.getMaxEnginesLimit() > 14) {
                            userInfo.setMaxUsersLimit(8400);
                            userInfo.setMaxEnginesLimit(14);
                        }
                        userInfoLabel.setText(userInfo.toString());

                    }
                }
            });


            BmTestManager.getInstance().testUserKeyNotificationListeners.add(new ITestUserKeyNotification() {
                @Override
                public void onTestUserKeyChanged(String userKey) {
                    setUserKey(userKey);
                    signUpToBlazemeterButton.setEnabled(!(userKey.matches(Constants.USERKEY_REGEX) & BmTestManager.getInstance().isUserKeyValid()));
                }
            });

            testIdComboBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    BmTestManager bmTestManager = BmTestManager.getInstance();
                    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                        Object selectedTest = testIdComboBox.getSelectedItem();
                        if (selectedTest instanceof TestInfo) {
                            TestInfo testInfo = (TestInfo) selectedTest;
                            if (!testInfo.getName().equals(Constants.NEW) & !testInfo.getName().isEmpty()) {
                                bmTestManager.setTestInfo(testInfo);
                            }
                        } else if (selectedTest.toString().equals(Constants.NEW)) {
                            testIdComboBox.setSelectedItem(Constants.NEW);
                            configureMainPanelControls(null);
                            cloudPanel.reset();
                            Utils.enableElements(cloudPanel, false);
                            TestInfo testInfo = new TestInfo();
                            testInfo.setName(Constants.NEW);
                            bmTestManager.setTestInfo(testInfo);
                        }
                    }
                }
            });

            if (bmTestManager.isUserKeyFromProp()) {
                String key = bmTestManager.getUserKey();
                if (key.length() >= 20)
                    key = key.substring(0, 5) + "**********" + key.substring(14, key.length());
                setUserKey(key);
                userKeyTextField.setEnabled(false);
                userKeyTextField.setToolTipText("User key found in jmeter.properties file");
                signUpToBlazemeterButton.setVisible(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fetchUserTestsAsync();

                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BmTestManager.getInstance().getUserInfo();
                    }
                }).start();
            } else {
                String userKey = BmTestManager.getInstance().getUserKey();
                if (!userKey.isEmpty()) {
                    signUpToBlazemeterButton.setVisible(false);
                    userKeyTextField.setText(userKey);
                    fetchUserTestsAsync();

                }
                userKeyTextField.addFocusListener(new FocusListener() {
                    String oldVal = "";

                    @Override
                    public void focusGained(FocusEvent focusEvent) {
                        oldVal = userKeyTextField.getText();
                    }

                    @Override
                    public void focusLost(FocusEvent focusEvent) {
                        String newVal = userKeyTextField.getText();
                        if (!newVal.equals(oldVal)) {
                            BmTestManager bmTestManager = BmTestManager.getInstance();
                            bmTestManager.setUserKey(newVal);
                            if (!newVal.isEmpty()) {
                                fetchUserTestsAsync();
                                signUpToBlazemeterButton.setVisible(false);
                            }
                        }
                    }
                });
            }
            //Here should be all changes of TestInfo processed
            bmTestManager.testInfoNotificationListeners.add(new ITestInfoNotification() {
                @Override
                public void onTestInfoChanged(TestInfo testInfo) {
                    if (testInfo == null) {
                        return;
                    }
                    if (testInfo.getError() != null) {
                        String errorTitle = "Problems with test";
                        String errorMessage = testInfo.getError();
                        if (errorMessage.equals("Insufficient credits")) {
                            errorMessage = errorMessage + ": turn to customer support service";
                        }
                        if (errorMessage.equals("Test not found")) {
                            testInfo.setError(null);
                            return;
                        }
                        JMeterUtils.reportErrorToUser(errorMessage, errorTitle);
                        testInfo.setError(null);
                    }
                    if (testInfo.getStatus() == TestStatus.Running) {
                        runLocal.setEnabled(false);
                        runRemote.setEnabled(false);
                        Utils.enableElements(jMeterPropertyPanel, false);
                    }

                    if ((testInfo.getStatus() == TestStatus.NotRunning)) {
                        Utils.enableElements(jMeterPropertyPanel, true);
                        boolean isTestRunning = BmTestManager.isTestRunning();
                        runLocal.setEnabled(!isTestRunning);
                        runRemote.setEnabled(!isTestRunning);

                        configureMainPanelControls(testInfo);

                        if (BmTestManager.getInstance().getIsLocalRunMode() & BmTestManager.isTestRunning()) {
                            try {
                                String[] jmeterEngines = LocateRegistry.getRegistry(Registry.REGISTRY_PORT).list();
                                if (jmeterEngines[0].equals("JMeterEngine")) {
                                    JToolBar jToolBar = GuiPackage.getInstance().getMainToolbar();
                                    Component[] components = jToolBar.getComponents();
                                    ActionRouter.getInstance().actionPerformed(new ActionEvent(components[0], ActionEvent.ACTION_PERFORMED, ActionNames.REMOTE_STOP_ALL));
                                }

                            } catch (ConnectException ce) {
                                BmLog.error("Failed to connect to RMI registry: jmeter is running in non-distributed mode");
                                StandardJMeterEngine.stopEngine();
                            } catch (RemoteException re) {
                                BmLog.error("Failed to get list of remote objects from RMI registry: jmeter is running in non-distributed mode");
                            }
                        }
                    }

                    setTestInfo(testInfo);

                    if ((!testInfo.getName().equals(Constants.NEW)) & (!testInfo.getName().isEmpty())) {
                        String currentTest = JMeterUtils.getPropDefault(Constants.CURRENT_TEST, "");
                        String currentTestId = null;
                        if (!currentTest.isEmpty()) {
                            currentTestId = currentTest.substring(0, currentTest.indexOf(";"));
                        } else {
                            currentTestId = "";
                        }
                        if (testInfo != null && !currentTestId.equals(testInfo.getId())) {
                            JMeterUtils.setProperty(Constants.CURRENT_TEST, testInfo.getId() + ";" + testInfo.getName());
                            TestInfoController.stop();

                        } else if (currentTestId.equals(testInfo.getId())) {
                            TestInfoController.start(testInfo.getId());

                        } else {
                            return;

                        }
                        TestInfoController.start(testInfo.getId());
                    }

                    if (testInfo.getName().equals(Constants.NEW) || (testInfo.getName().isEmpty())) {
                        TestInfoController.stop();
                    }

                }
            });
            //Processing serverStatusChangedNotification
            ServerStatusController serverStatusController = ServerStatusController.getServerStatusController();
            serverStatusController.serverStatusChangedNotificationListeners.add(new ServerStatusController.ServerStatusChangedNotification() {
                @Override
                public void onServerStatusChanged() {
                    ServerStatusController.ServerStatus serverStatus = ServerStatusController.getServerStatus();
                    switch (serverStatus) {
                        case AVAILABLE:
                            TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
                            TestInfoController.start(testInfo.getId());
                            boolean testIsRunning = testInfo.getStatus() == TestStatus.Running;
                            enableMainPanelControls(!testIsRunning);
                            Utils.enableElements(cloudPanel, !testIsRunning);
                            Utils.enableElements(jMeterPropertyPanel, !testIsRunning);
                            break;
                        case NOT_AVAILABLE:
                            enableMainPanelControls(false);
                            Utils.enableElements(jMeterPropertyPanel, false);
                            Utils.enableElements(jMeterPropertyPanel, false);
                            TestInfoController.stop();
                            break;
                    }

                }
            });

        }
    }


    private void fetchUserTestsAsync() {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        String userKey = bmTestManager.getUserKey();
        if (userKey == null || userKey.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Please enter user key", "No user key", JOptionPane.ERROR_MESSAGE);
            return;
        }
        testIdComboBox.removeAllItems();
        testIdComboBox.addItem("LOADING...");
        testIdComboBox.setEnabled(false);
        ITestListReceivedNotification testListReceivedNotification = new ITestListReceivedNotification() {
            @Override
            public void testReceived(ArrayList<TestInfo> tests) {
                testIdComboBox.removeAllItems();
                testIdComboBox.setEnabled(true);
                if (tests != null) {
                    testIdComboBox.removeAllItems();
                    testIdComboBox.addItem(Constants.NEW);
                    testIdComboBox.setSelectedItem(Constants.NEW);
                    BmTestManager.getInstance().setUserKeyValid(true);
                    java.util.List<String> testIdList = new ArrayList<String>();
                    // create list of tests on server
                    for (TestInfo ti : tests) {
                        addTestId(ti, false);
                        testIdList.add(ti.getId());
                    }
                    String[] curTest = StringUtils.split(JMeterUtils.getPropDefault(Constants.CURRENT_TEST, ""), ";");
                    String curTestId = null;
                    String curTestName = null;
                    try {
                        if (curTest.length > 0) {
                            curTestId = curTest[0];
                            curTestName = curTest[1];

                        }

                    } catch (ArrayIndexOutOfBoundsException iobe) {
                        BmLog.error("Current test property was not applied to screen: " + iobe);
                    }

                    boolean exists = false;

                    if (curTestId != null) {
                        for (int index = 1; index <= testIdComboBox.getItemCount() && !exists; index++) {
                            Object obj = testIdComboBox.getItemAt(index);
                            if (obj instanceof TestInfo & obj != null) {
                                TestInfo ti = (TestInfo) testIdComboBox.getItemAt(index);
                                exists = curTestId.equals(ti.getId());
                            }
                        }
                    }
                    //add current test to testIdComboBox if it is present in tests from server
                    if (!exists & testIdList.contains(curTestId)) {
                        testIdComboBox.addItem(curTestId + " - " + curTestName);
                    }

                    // select current test(which was previously selected in testIdComboBox)
                    if (curTest.length != 0) {
                        for (TestInfo ti : tests) {
                            if (ti.getId().equals(curTestId)) {
                                testIdComboBox.setSelectedItem(ti);
                            }
                        }
                        if ((!testIdList.isEmpty() & !curTestId.isEmpty()) && !testIdList.contains(curTestId)) {
                            JMeterUtils.reportErrorToUser("Test=" + curTestId + " was not found on server. Select test from list."
                                    , "Test was not found on server");
                            JMeterUtils.setProperty(Constants.CURRENT_TEST, "");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Please enter valid user key", "Invalid user key", JOptionPane.ERROR_MESSAGE);
                    BmTestManager.getInstance().setUserKeyValid(false);
                    cloudPanel.reset();
                    Utils.enableElements(cloudPanel, false);
                    testIdComboBox.setSelectedItem(Constants.EMPTY);
                }
            }
        };
        BmTestManager.getInstance().getTestsAsync(userKey, testListReceivedNotification);
    }

    public void setUserKey(String key) {
        if (key.isEmpty()) {
            return;
        }
        userKeyTextField.setText(key);
        BmLog.debug("Setting user key " + key);
    }

    public String getUserKey() {
        return userKeyTextField.getText();
    }

    public void addTestId(Object test, boolean selected) {
        testIdComboBox.addItem(test);
        if (selected) {
            testIdComboBox.setSelectedItem(test);
        }
    }


    private void runModeChanged(boolean isLocalRunMode) {
        runLocal.setSelected(isLocalRunMode);
        runRemote.setSelected(!isLocalRunMode);
        cloudPanel.setVisible(!isLocalRunMode);
        jMeterPropertyPanel.setVisible(!isLocalRunMode);
    }

    protected void setTestInfo(TestInfo testInfo) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        if (testInfo == null || testInfo.isEmpty() || !testInfo.isValid()) {
            testInfo = new TestInfo();
            testInfo.setName(Constants.NEW);
            testIdComboBox.setSelectedItem(testInfo.getName());
            configureMainPanelControls(null);
        } else {
            testIdComboBox.setSelectedItem(testInfo);
            configureMainPanelControls(testInfo);
            runModeChanged(bmTestManager.getIsLocalRunMode());
        }
        if (!bmTestManager.getIsLocalRunMode()) {
            // update Cloud panel
            cloudPanel.setTestInfo(testInfo);
        }
    }


    private void configureMainPanelControls(TestInfo testInfo) {
        boolean isRunning = (testInfo != null && testInfo.getStatus() == TestStatus.Running);

        if (testInfo != null) {
            testNameArea.setText(testInfo.getName());
            testIdArea.setText(testInfo.getId());
            createNewButton.setEnabled(!isRunning);
            goToTestPageButton.setEnabled(!testInfo.getId().isEmpty());
        } else {
            testNameArea.setText(Constants.EMPTY);
            testIdArea.setText(Constants.EMPTY);
            createNewButton.setEnabled(!isRunning);
            goToTestPageButton.setEnabled(false);
        }
        if (!BmTestManager.getInstance().isUserKeyFromProp()) {
            userKeyTextField.setEnabled(!isRunning);
        }
        testIdComboBox.setEnabled(!isRunning & testIdComboBox.getItemCount() > 0);
        reloadButton.setEnabled(!isRunning);
    }

    public static TestPanel getGui() {
        if (gui == null) {
            gui = new TestPanel();
        }
        return gui;
    }

    public JPanel getjMeterPropertyPanel() {
        return jMeterPropertyPanel;
    }


    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setAutoscrolls(true);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 3, new Insets(1, 1, 1, 1), -1, -1));
        panel1.setVisible(true);
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder("Info"));
        final JLabel label1 = new JLabel();
        label1.setText("User Key");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Test Info");
        panel1.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Select Test");
        panel1.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        helpButton = new JButton();
        helpButton.setEnabled(true);
        helpButton.setFont(new Font("Tahoma", helpButton.getFont().getStyle(), 16));
        helpButton.setHideActionText(false);
        helpButton.setHorizontalAlignment(0);
        helpButton.setHorizontalTextPosition(0);
        helpButton.setIcon(new ImageIcon(getClass().getResource("/com/blazemeter/jmeter/images/question.png")));
        helpButton.setInheritsPopupMenu(true);
        helpButton.setMaximumSize(new Dimension(22, 22));
        helpButton.setMinimumSize(new Dimension(20, 20));
        helpButton.setPreferredSize(new Dimension(20, 20));
        helpButton.setText("");
        helpButton.setToolTipText("Help");
        panel2.add(helpButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        userInfoLabel = new JLabel();
        userInfoLabel.setText(Constants.EMPTY);
        panel2.add(userInfoLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userKeyTextField = new JTextField();
        userKeyTextField.setText("Enter your user key");
        userKeyTextField.setToolTipText("User key - can be found on your profile page , click \"?\" button for more info");
        panel2.add(userKeyTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Border border = BorderFactory.createLineBorder(Color.GRAY, 1);
        Color color = new Color(240, 240, 240);
        testIdArea = new JTextArea(Constants.EMPTY, 1, 1);
        testIdArea.setBorder(border);
        testIdArea.setBackground(color);
        testIdArea.setEnabled(true);
        testIdArea.setToolTipText("Test id of current test");
        panel3.add(testIdArea, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), new Dimension(70, -1), new Dimension(70, -1), 0, false));
        testNameArea = new JTextArea(Constants.EMPTY, 1, 1);
        testNameArea.setBackground(color);
        testNameArea.setAutoscrolls(false);
        testNameArea.setBorder(border);
        testNameArea.setEnabled(true);
        testNameArea.setToolTipText("Test name of current test");
        panel3.add(testNameArea, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createNewButton = new JButton();
        createNewButton.setEnabled(true);
        createNewButton.setFont(new Font("Tahoma", createNewButton.getFont().getStyle(), 16));
        createNewButton.setHideActionText(false);
        createNewButton.setHorizontalAlignment(0);
        createNewButton.setHorizontalTextPosition(0);
        createNewButton.setIcon(new ImageIcon(getClass().getResource("/com/blazemeter/jmeter/images/plus.png")));
        createNewButton.setMaximumSize(new Dimension(22, 22));
        createNewButton.setMinimumSize(new Dimension(20, 20));
        createNewButton.setPreferredSize(new Dimension(20, 20));
        createNewButton.setText("");
        createNewButton.setToolTipText("Create new test");
        panel3.add(createNewButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        signUpToBlazemeterButton = new JButton();
        signUpToBlazemeterButton.setActionCommand("Sign up to BlazeMeter!");
        signUpToBlazemeterButton.setLabel("Sign up to BlazeMeter's free trial!");
        signUpToBlazemeterButton.setText("Sign up to BlazeMeter's free trial!");
        signUpToBlazemeterButton.setToolTipText("Register/Login to BlazeMeter site and find your User Key on a profile page.");
        panel3.add(signUpToBlazemeterButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(300, -1), new Dimension(300, -1), 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testIdComboBox = new JComboBox();
        testIdComboBox.setDoubleBuffered(true);
        testIdComboBox.setEditable(false);
        testIdComboBox.setEnabled(false);
        testIdComboBox.setToolTipText("Enter test id  or select test from list , click refresh button if you want to load test list from the server.");
        panel4.add(testIdComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        reloadButton = new JButton();
        reloadButton.setActionCommand("");
        reloadButton.setEnabled(true);
        reloadButton.setFont(new Font("Tahoma", reloadButton.getFont().getStyle(), 16));
        reloadButton.setHorizontalTextPosition(0);
        reloadButton.setIcon(new ImageIcon(getClass().getResource("/com/blazemeter/jmeter/images/refresh.png")));
        reloadButton.setMaximumSize(new Dimension(20, 20));
        reloadButton.setMinimumSize(new Dimension(20, 20));
        reloadButton.setPreferredSize(new Dimension(20, 20));
        reloadButton.setText("");
        reloadButton.setToolTipText("Reload tests list from server");
        reloadButton.setVerticalAlignment(0);
        panel4.add(reloadButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        goToTestPageButton = new JButton();
        goToTestPageButton.setActionCommand("Go to Test Page!");
        goToTestPageButton.setEnabled(true);
        goToTestPageButton.setHideActionText(false);
        goToTestPageButton.setLabel("Go to Test Page!");
        goToTestPageButton.setText("Go to Test Page!");
        goToTestPageButton.setToolTipText("Navigate to test page on Blazemeter site");
        goToTestPageButton.setVerifyInputWhenFocusTarget(false);
        goToTestPageButton.setVisible(true);
        panel4.add(goToTestPageButton, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(165, -1), new Dimension(165, -1), new Dimension(165, -1), 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Run Mode");
        panel1.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runLocal = new JRadioButton();
        runLocal.setSelected(false);
        runLocal.setText("Locally (Reporting Only)");
        runLocal.setToolTipText("Send results to cloud for displaying");
        panel5.add(runLocal, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        runRemote = new JRadioButton();
        runRemote.setSelected(true);
        runRemote.setText("Run in the Cloud");
        runRemote.setToolTipText("Using cloud engines for test");
        panel5.add(runRemote, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        mainPanel.add(jMeterPropertyPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jMeterPropertyPanel.setBorder(BorderFactory.createTitledBorder("JMeter Properties"));
        label1.setLabelFor(userKeyTextField);
        label2.setLabelFor(testNameArea);
        label3.setLabelFor(testIdComboBox);
        label4.setLabelFor(testNameArea);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(runRemote);
        buttonGroup.add(runLocal);
        cloudPanel = new CloudPanel();
        mainPanel.add(cloudPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    }
}
