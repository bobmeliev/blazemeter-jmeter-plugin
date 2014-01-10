package com.blazemeter.jmeter.testexecutor.panels;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.listeners.*;
import com.blazemeter.jmeter.testexecutor.notifications.*;
import com.blazemeter.jmeter.testexecutor.notificationsImpl.*;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.GuiUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/29/12
 * Time: 12:29
 */
public class TestPanel {
    private static TestPanel testPanel;

    //Gui controls
    private JTextField userKeyTextField;
    private JComboBox testIdComboBox;
    private JPanel mainPanel;
    private JTextArea testIdArea;
    private JTextArea testNameArea;
    private JButton reloadButton;
    private JButton signUpButton;
    private JButton createNewButton;
    private JButton goToTestPageButton;
    private JButton helpButton;
    private CloudPanel cloudPanel;
    private JRadioButton runRemote;
    private JRadioButton runLocal;
    private JLabel userInfoLabel;
    private AdvancedPropertiesPane advancedPropertiesPane;

    public TestPanel() {
        try {
            createMainPanel();
            reloadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    GuiUtils.getUserTests(testIdComboBox, mainPanel, cloudPanel, userKeyTextField.getText());

                }
            });
            ITestUserKeyNotification userKeyNotification = new TestUserKeyNotification(signUpButton,
                    testIdComboBox,
                    mainPanel,
                    cloudPanel);
            BmTestManager.getInstance().testUserKeyNotificationListeners.add(userKeyNotification);

            signUpButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    GuiUtils.Navigate(BmTestManager.getServerUrl() + "/user");
                }
            });

            CreateNewButtonListener createNewButtonListener = new CreateNewButtonListener(testNameArea,
                    mainPanel,
                    cloudPanel,
                    testIdComboBox);

            createNewButton.addActionListener(createNewButtonListener);

            GoToTestButtonListener goToTestButtonListener = new GoToTestButtonListener();

            goToTestPageButton.addActionListener(goToTestButtonListener);

        } catch (NullPointerException npe) {
            BmLog.error("Failed to construct TestPanel instance: " + npe);
        }
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void enableMainPanelControls(boolean isEnabled) {
        testIdComboBox.setEnabled(isEnabled);
        reloadButton.setEnabled(userKeyTextField.getText().equals(Constants.ENTER_YOUR_USER_KEY) ? false : isEnabled);
        userKeyTextField.setEnabled(isEnabled);
        createNewButton.setEnabled(isEnabled);
        goToTestPageButton.setEnabled(isEnabled);
    }


    /**
     * Here some heavy GUI listeners are initialized;
     */
    public void init() {
        BmTestManager bmTestManager = BmTestManager.getInstance();

        if (!JMeterUtils.getPropDefault(Constants.BLAZEMETER_TESTPANELGUI_INITIALIZED, false)) {
            JMeterUtils.setProperty(Constants.BLAZEMETER_TESTPANELGUI_INITIALIZED, "true");

            final IRunModeChangedNotification runModeChanged = new RunModeChangedNotification(runLocal,
                    runRemote,
                    cloudPanel,
                    advancedPropertiesPane);
            BmTestManager.getInstance().runModeChangedNotificationListeners.add(runModeChanged);

            RunModeListener runModeListener = new RunModeListener(runModeChanged);

            runLocal.addActionListener(runModeListener);
            runRemote.addActionListener(runModeListener);


            signUpButton.setEnabled(bmTestManager.getUserKey() == null || bmTestManager.
                    getUserKey().
                    isEmpty());

            IUserInfoChangedNotification userInfoChangedNotification = new UserInfoChangedNotification(userInfoLabel, testIdComboBox);
            BmTestManager.getInstance().userInfoChangedNotificationListeners.add(userInfoChangedNotification);


            TestIdComboBoxListener comboBoxListener = new TestIdComboBoxListener(testIdComboBox, cloudPanel);
            testIdComboBox.addItemListener(comboBoxListener);

            // init userKey
            if (bmTestManager.isUserKeyFromProp()) {
                String key = bmTestManager.getUserKey();
                if (key.length() >= 20)
                    key = key.substring(0, 5) + "**********" + key.substring(14, key.length());
                setUserKey(key);
                userKeyTextField.setEnabled(false);
                userKeyTextField.setToolTipText("User key found in jmeter.properties file");
                signUpButton.setVisible(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GuiUtils.getUserTests(testIdComboBox, mainPanel, cloudPanel, userKeyTextField.getText());


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
                    signUpButton.setVisible(false);
                    userKeyTextField.setText(userKey);
                    GuiUtils.getUserTests(testIdComboBox, mainPanel, cloudPanel, userKeyTextField.getText());
                }
                UserKeyListener userKeyListener = new UserKeyListener(userKeyTextField,
                        signUpButton, testIdComboBox, mainPanel, cloudPanel);
                userKeyTextField.getDocument().addDocumentListener(userKeyListener);

            }
            // init userKey

            //Here should be all changes of TestInfo processed
            ITestInfoNotification testInfoNotification = new TestInfoNotificationTP(runLocal, runRemote,
                    advancedPropertiesPane.getjMeterPropertyPanel());
            bmTestManager.testInfoNotificationListeners.add(testInfoNotification);

            //Processing serverStatusChangedNotification
            ServerStatusController serverStatusController = ServerStatusController.getServerStatusController();
            IServerStatusChangedNotification serverStatusChangedNotification = new ServerStatusChangedNotificationTP(this, cloudPanel,
                    (PropertyPanel) advancedPropertiesPane.getjMeterPropertyPanel());
            serverStatusController.serverStatusChangedNotificationListeners.add(serverStatusChangedNotification);


        }
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


    public void setTestInfo(TestInfo testInfo) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        if (testInfo == null || testInfo.isEmpty() || !testInfo.isValid()) {
            testInfo = new TestInfo();
            testInfo.setName(Constants.NEW);
            testIdComboBox.setSelectedItem(testInfo.getName());
            configureMainPanel(null);
        } else {
            testIdComboBox.setSelectedItem(testInfo);
            configureMainPanel(testInfo);
            GuiUtils.runModeChanged(runLocal, runRemote, cloudPanel,
                    advancedPropertiesPane,
                    bmTestManager.getIsLocalRunMode());
        }
        if (!bmTestManager.getIsLocalRunMode()) {
            // update Cloud panel
            cloudPanel.setTestInfo(testInfo);
        }
    }


    public void configureMainPanel(TestInfo testInfo) {
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

    public static TestPanel getTestPanel() {
        if (testPanel == null) {
            testPanel = new TestPanel();
        }
        return testPanel;
    }

    public AdvancedPropertiesPane getAdvancedPropertiesPane() {
        return advancedPropertiesPane;
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
        testIdArea.setEditable(false);
        testIdArea.setToolTipText("Test id of current test");
        panel3.add(testIdArea, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), new Dimension(70, -1), new Dimension(70, -1), 0, false));
        testNameArea = new JTextArea(Constants.EMPTY, 1, 1);
        testNameArea.setEditable(false);
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
        signUpButton = new JButton();
        signUpButton.setActionCommand("Sign up to BlazeMeter!");
        signUpButton.setLabel("Sign up to BlazeMeter's free trial!");
        signUpButton.setText("Sign up to BlazeMeter's free trial!");
        signUpButton.setToolTipText("Register/Login to BlazeMeter site and find your User Key on a profile page.");
        panel3.add(signUpButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(300, -1), new Dimension(300, -1), 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        testIdComboBox = new JComboBox(comboBoxModel);
        testIdComboBox.setDoubleBuffered(true);
        testIdComboBox.setEditable(false);
        testIdComboBox.setEnabled(false);
        testIdComboBox.setToolTipText("Enter test id  or select test from list , click refresh button if you want to load test list from the server.");
        panel4.add(testIdComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        reloadButton = new JButton();
        reloadButton.setActionCommand("");
        reloadButton.setEnabled(false);
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
        advancedPropertiesPane = new AdvancedPropertiesPane();
        mainPanel.add(advancedPropertiesPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }
}