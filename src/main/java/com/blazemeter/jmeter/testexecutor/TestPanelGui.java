package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.common.BlazemeterApi;
import com.blazemeter.jmeter.common.BmLog;
import com.blazemeter.jmeter.common.TestStatus;
import com.blazemeter.jmeter.common.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/29/12
 * Time: 12:29
 */
public class TestPanelGui {
    private static final String NEW_TEST_ID = "---NEW---";
    private static final String HELP_URL = "http://community.blazemeter.com/knowledgebase/articles/83191-blazemeter-plugin-to-jmeter#user_key";
    private static final String SELECT_TEST = "Please, select test from list";
    private static final String LOADING_TEST_INFO = "Loading test info, please wait";
    private static final String CAN_NOT_BE_RUN = "This test could not be run from Jmeter Plugin. Please, select another one from the list above.";
    private static final String TEST_INFO_IS_LOADED = "Test info is loaded";
    private static long lastCloudPanelUpdate = 0;
    private JTextField userKeyTextField;
    private JTextField reportNameTextField;
    private JTextField testNameTextField;
    private JComboBox testIdComboBox;



    private JPanel mainPanel;
    private JTextField testIdTextField;
    private JButton reloadButton;
    private JButton signUpToBlazemeterButton;
    private JButton createNewButton;
    private JButton goToTestPageButton;
    private JButton helpButton;
    private JSlider numberOfUsersSlider;
    private JTextField numberOfUserTextBox;
    private JTextField enginesDescription;
    private JComboBox locationComboBox;
    private JPanel cloudPanel;
    private JButton runInTheCloud;
    private JSpinner iterationsSpinner;
    private JSpinner durationSpinner;
    private JSpinner rampupSpinner;
    private JRadioButton runRemote;
    private JRadioButton runLocal;
    private JPanel localPanel;
    private JPanel overridesPanel;
    private JPanel infoPanel;
    private JLabel infoLabel;
    private JLabel userInfoLabel;
    private JButton addFilesButton;


    public TestPanelGui() {
        if (BmTestManager.getInstance().isUserKeyFromProp()) {
            String key = BmTestManager.getInstance().getUserKey();
            if (key.length() >= 20)
                key = key.substring(0, 5) + "**********" + key.substring(14, key.length());
            setUserKey(key);
            userKeyTextField.setEnabled(false);
            userKeyTextField.setToolTipText("User key found in jmeter.properties file");
            signUpToBlazemeterButton.setVisible(false);
            fetchUserTestsAsync();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BmTestManager.getInstance().getUserInfo();
                }
            });
        } else {
            userKeyTextField.addFocusListener(new FocusListener() {
                String oldVal = "";

                @Override
                public void focusGained(FocusEvent focusEvent) {
                    oldVal = userKeyTextField.getText();
                }

                @Override
                public void focusLost(FocusEvent focusEvent) {
                    String newVal = userKeyTextField.getText();
                    signUpToBlazemeterButton.setEnabled(newVal.isEmpty());
                    if (!newVal.equals(oldVal)) {
                        BmTestManager.getInstance().setUserKey(newVal);
                        if (!newVal.isEmpty())
                            fetchUserTestsAsync();
                    }
                }
            });
        }


        testIdComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    Object selected = testIdComboBox.getSelectedItem();
                    if (selected instanceof TestInfo) {
                        TestInfo testInfo = (TestInfo) selected;
                        BmTestManager.getInstance().setTestInfo(testInfo);
                    } else if (Utils.isInteger(selected.toString())) {
                        TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(BmTestManager.getInstance().getUserKey(),
                                selected.toString(), true);
                        BmLog.console(ti.toString());
                        if (ti.status == TestStatus.Running || ti.status == TestStatus.NotRunning) {
                            BmTestManager.getInstance().setTestInfo(ti);
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, ti.error, "Test not found error", JOptionPane.ERROR_MESSAGE);
                            configureFields(null);
                        }
                    } else if (selected.toString().equals(NEW_TEST_ID)) {
                        setTestInfo(null);
                        configureFields(null);
                    }
                }
            }
        });


        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fetchUserTestsAsync();
            }
        });

        signUpToBlazemeterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.Navigate(BlazemeterApi.BmUrlManager.getServerUrl() + "/user");
            }
        });

        createNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String userKey = BmTestManager.getInstance().getUserKey();
                if (userKey == null || userKey.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "Please enter user key", "No user key", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String testName = testNameTextField.getText().trim();
                if (testName.isEmpty()) {
                    testName = JOptionPane.showInputDialog(mainPanel, "Please enter valid test name!");
                    if (testName == null || testName.trim().isEmpty())
                        return;
                }
                TestInfo ti = BlazemeterApi.getInstance().createTest(userKey, testName);
                if (ti != null && ti.status == null) {
                    addTestId(ti, true);
                }

                BmTestManager.getInstance().uploadJmx();

            }
        });

        BmTestManager.getInstance().testInfoNotificationListeners.add(new BmTestManager.TestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                if (testIdComboBox.getItemCount() == 1) {
                    addTestId(testInfo, true);
                }
                setTestInfo(testInfo);
                updateCloudPanel();
            }
        });

        BmTestManager.getInstance().testUserKeyNotificationListeners.add(new BmTestManager.TestUserKeyNotification() {
            @Override
            public void onTestUserKeyChanged(String userKey) {
                setUserKey(userKey);
                signUpToBlazemeterButton.setEnabled(userKey == null || userKey.isEmpty());
            }
        });

        BmTestManager.getInstance().userInfoChangedNotificationListeners.add(new BmTestManager.UserInfoChanged() {
            @Override
            public void onUserInfoChanged(UserInfo userInfo) {
                if (userInfo == null) {
                    userInfoLabel.setText("");
                    clearTestInfo();
                } else {
                    userInfoLabel.setText(userInfo.toString());
                    numberOfUsersSlider.setMaximum(userInfo.maxUsersLimit);
                }
            }
        });
        signUpToBlazemeterButton.setEnabled(BmTestManager.getInstance().getUserKey() == null || BmTestManager.
                getInstance().
                getUserKey().
                isEmpty());

        reportNameTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                String name = reportNameTextField.getText().trim();
                String validName = getValidReportName(name);
                if (!name.equals(validName)) {
                    reportNameTextField.setText(validName);
                }
            }
        });


        BmTestManager.getInstance().statusChangedNotificationListeners.add(new BmTestManager.StatusChangedNotification() {
            @Override
            public void onTestStatusChanged() {
                TestInfo ti = BmTestManager.getInstance().getTestInfo();
                switch (ti.status) {
                    case Running:
                        runLocal.setEnabled(false);
                        runRemote.setEnabled(false);
                        break;
                    case NotRunning:
                        runLocal.setEnabled(true);
                        runRemote.setEnabled(true);
                        configureFields(ti);
                        break;
                    /*case NotFound:        add actions to cases in below
                        JOptionPane.showMessageDialog(mainPanel, ti.error, "Test not found error", JOptionPane.ERROR_MESSAGE);
                        break;
                    case Error:
                        JOptionPane.showMessageDialog(mainPanel, ti.error, "Error", JOptionPane.ERROR_MESSAGE);
                        break;*/
                }
            }
        });

        configureFields(BmTestManager.getInstance().getTestInfo());

        goToTestPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String url = BmTestManager.getInstance().getTestUrl();
                if (url != null)
                    Utils.Navigate(url);
            }
        });
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.Navigate(HELP_URL);
            }
        });
        addTestId(NEW_TEST_ID, true);
        numberOfUserTextBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                int numberOfUsers = 0;
                try {
                    numberOfUsers = Integer.valueOf(numberOfUserTextBox.getText().trim());
                } catch (NumberFormatException e) {
                    BmLog.error("You've tried to enter not integer. Please, correct mistake!");
                    numberOfUsers = 0;
                } finally {
                    numberOfUsersSlider.setValue(numberOfUsers);
                }
            }
        });

        numberOfUsersSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int numberOfUsers = numberOfUsersSlider.getValue();
                int engines = 1;
                int userPerEngine;
                String description;
                if (numberOfUsers <= 300) {
                    numberOfUsers = numberOfUsers < 1 ? 1 : numberOfUsers;
                    description = "MEDIUM engine";
                } else if (numberOfUsers <= 2400) {
                    engines = numberOfUsers / 300;
                    if (numberOfUsers % 300 > 0) {
                        engines++;
                    }
                    description = "MEDIUM engines";
                } else {

                    engines = numberOfUsers / 600;
                    if (numberOfUsers % 600 > 0) {
                        engines++;
                    }
                    description = "LARGE engines";
                }
                userPerEngine = numberOfUsers / engines;
                enginesDescription.setText(String.format("%d %s x %d users", engines, description, userPerEngine));
                numberOfUserTextBox.setText(Integer.toString(userPerEngine * engines));
            }
        });

        runInTheCloud.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogButton;

                if ("start".equals(e.getActionCommand().toLowerCase())) {
                    BmTestManager bmTestManager = BmTestManager.getInstance();
                    TestInfo ti = bmTestManager.getTestInfo();
                    if (ti.status == TestStatus.NotFound) {
                        JOptionPane.showMessageDialog(mainPanel, "Test is not selected", "Select a test", JOptionPane.ERROR_MESSAGE);
                    } else {
                        dialogButton = JOptionPane.showConfirmDialog(mainPanel, "Do you want to start test?", "Start test?",
                                JOptionPane.YES_NO_OPTION);
                        if (dialogButton == JOptionPane.YES_OPTION) {
                            startInTheCloud();
                            runLocal.setEnabled(false);
                            enableCloudControls(false);
                        }

                    }


                } else {
                    dialogButton = JOptionPane.showConfirmDialog(mainPanel, "Do you want to stop test?", "Stop test?",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogButton == JOptionPane.YES_OPTION) {
                        BmTestManager bmTestManager = BmTestManager.getInstance();
                        BmTestManager.getInstance().stopInTheCloud();
                        configureFields(bmTestManager.getTestInfo());
                        runLocal.setEnabled(true);
                        enableCloudControls(true);
                    }

                }
                updateCloudPanel();
            }
        });


        rampupSpinner.setModel(new SpinnerNumberModel(0, 0, 3600, 60));
        iterationsSpinner.setModel(new SpinnerNumberModel(0, 0, 1010, 1));
        durationSpinner.setModel(new SpinnerNumberModel(0, 0, 480, 60));

        final BmTestManager.RunModeChanged runModeChanged = new BmTestManager.RunModeChanged() {
            @Override
            public void onRunModeChanged(boolean isLocalRunMode) {
                runLocal.setSelected(isLocalRunMode);
                runRemote.setSelected(!isLocalRunMode);
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


        addFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String url = BmTestManager.getInstance().getTestUrl() + "/files";
                if (url != null)
                    Utils.Navigate(url);
            }
        });
    }
    public JPanel getMainPanel() {
           return mainPanel;
       }

    private void startInTheCloud() {
        saveCloudTest();
        int id = BmTestManager.getInstance().runInTheCloud();
        if (id != -1) {

            String url = BmTestManager.getInstance().getTestUrl();
            if (url != null)
                url = url.substring(0, url.length() - 5);
            Utils.Navigate(url);
        }
        if (id == -2) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Select test from drop-down list and \n try again", "testid is empty",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(BmTestManager.getInstance().getUserKey(),
                BmTestManager.getInstance().getTestInfo().id, true);
        configureFields(ti);
        BmTestManager.getInstance().setTestInfo(ti);

    }

    private void enableCloudControls(boolean isEnabled) {
        locationComboBox.setEnabled(isEnabled);
        numberOfUsersSlider.setEnabled(isEnabled);
        numberOfUserTextBox.setEnabled(isEnabled);
        rampupSpinner.setEnabled(isEnabled);
        iterationsSpinner.setEnabled(isEnabled);
        durationSpinner.setEnabled(isEnabled);
        addFilesButton.setEnabled(isEnabled);
    }

    private void saveCloudTest() {
        int numberOfUsers = numberOfUsersSlider.getValue();
        int engines;
        int userPerEngine;
        String engineSize = "m1.medium";
        if (numberOfUsers <= 300) {
            engines = 0;
            userPerEngine = numberOfUsers == 0 ? 1 : numberOfUsers;
        } else {
            if (numberOfUsers <= 2400) {
                engines = numberOfUsers / 300;
                if (numberOfUsers % 300 > 0) {
                    engines++;
                }
            } else {
                engines = numberOfUsers / 600;
                if (numberOfUsers % 600 > 0) {
                    engines++;
                }
                engineSize = "m1.large";
            }
            userPerEngine = numberOfUsers / engines;
        }


        int iterations = Integer.parseInt(iterationsSpinner.getValue().toString());
        iterations = iterations > 0 || iterations < 1001 ? iterations : -1;

        int rumpUp = Integer.parseInt(rampupSpinner.getValue().toString());
        int duration = Integer.parseInt(durationSpinner.getValue().toString());
        duration = duration > 0 ? duration : -1;
        String location = locationComboBox.getSelectedItem().toString();

        BlazemeterApi.getInstance().updateTestSettings(BmTestManager.getInstance().getUserKey(),
                BmTestManager.getInstance().getTestInfo().id,
                location, engines, engineSize, userPerEngine, iterations, rumpUp, duration);
    }

    private void clearTestInfo() {
        testIdComboBox.removeAllItems();
        BmTestManager.getInstance().setTestInfo(null);
    }

    private void fetchUserTestsAsync() {
        String userKey = BmTestManager.getInstance().getUserKey();
        if (userKey == null || userKey.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Please enter user key", "No user key", JOptionPane.ERROR_MESSAGE);
            return;
        }
        testIdComboBox.removeAllItems();
        testIdComboBox.addItem("LOADING...");
        testIdComboBox.setEnabled(false);
        BlazemeterApi.getInstance().getTestsAsync(userKey, new BlazemeterApi.TestContainerNotifier() {
            @Override
            public void testReceived(ArrayList<TestInfo> tests) {
                testIdComboBox.removeAllItems();
                testIdComboBox.setEnabled(true);
                testIdComboBox.addItem(NEW_TEST_ID);
                if (tests != null) {
                    for (TestInfo testInfo : tests) {
                        addTestId(testInfo, false);
                    }

                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Please enter valid user key", "Invalid user key", JOptionPane.ERROR_MESSAGE);
                }
                setTestInfo(BmTestManager.getInstance().getTestInfo());
                updateCloudPanel();
            }

        });


    }

    public void setUserKey(String key) {
        userKeyTextField.setText(key);
        BmLog.console("set key" + key);
    }

    public void addTestId(Object test, boolean selected) {
        testIdComboBox.addItem(test);
        if (selected) {
            testIdComboBox.setSelectedItem(test);
        }
    }


    private void runModeChanged(boolean isLocalRun) {
        if (isLocalRun) {
            localPanel.setVisible(true);
            cloudPanel.setVisible(false);
            infoPanel.setVisible(false);
        } else {
            localPanel.setVisible(false);
            cloudPanel.setVisible(true);
            infoPanel.setVisible(true);
        }
    }

    protected void setTestInfo(TestInfo testInfo) {
        if (testInfo == null || testInfo.isEmpty() || !testInfo.isValid()) {
            testIdComboBox.setSelectedItem(NEW_TEST_ID);
            infoLabel.setText(SELECT_TEST);
            configureFields(null);
        } else {
            testIdComboBox.setSelectedItem(testInfo);
            configureFields(testInfo);
            infoLabel.setText(LOADING_TEST_INFO);
            runModeChanged(BmTestManager.getInstance().getIsLocalRunMode());
            infoLabel.setText(TEST_INFO_IS_LOADED);
        }
    }

    private Thread updateCloudPanelThread;

    protected void updateCloudPanel() {
        long now = new Date().getTime();
        if (lastCloudPanelUpdate + 1000 > now) {
            lastCloudPanelUpdate = now;
            return;
        }

        if (BmTestManager.getInstance().getIsLocalRunMode())
            return;

        interruptCloudPanelUpdate();
        updateCloudPanelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(BmTestManager.getInstance().getUserKey(),
                        BmTestManager.getInstance().getTestInfo().id, true);
                BmTestManager.getInstance().setTestInfo(ti);
                if (Thread.currentThread().isInterrupted())
                    return;
                if ("jmeter".equals(ti.type)) {
                    locationComboBox.setSelectedItem(ti.getLocation());
                    numberOfUsersSlider.setValue(ti.getNumberOfUsers());
                    rampupSpinner.setValue(ti.overrides.rampup);
                    iterationsSpinner.setValue(ti.overrides.iterations == -1 ? 0 : ti.overrides.iterations);
                    durationSpinner.setValue(ti.overrides.duration == -1 ? 0 : ti.overrides.duration);
                    runInTheCloud.setActionCommand(ti.status == TestStatus.Running ? "stop" : "start");
                    runInTheCloud.setText(ti.status == TestStatus.Running ? "Stop" : "Run in the Cloud!");
                } else {
                    infoLabel.setText(testIdComboBox.getSelectedItem().equals(NEW_TEST_ID) ? SELECT_TEST : CAN_NOT_BE_RUN);
                }
            }
        });
        updateCloudPanelThread.start();

    }

    private void interruptCloudPanelUpdate() {
        if (updateCloudPanelThread != null) {

            if (updateCloudPanelThread.isAlive()) {
                updateCloudPanelThread.interrupt();
                BmLog.console("Thread is still alive!");
            }
        }
    }

    private String getValidReportName(String name) {
        if (name == null || name.isEmpty()) {
            return "sample.jtl";
        }
        if (!name.toLowerCase().endsWith(".jtl")) {
            return name + ".jtl";
        }
        return name;
    }

    private void configureFields(TestInfo testInfo) {
        boolean isRunning = BmTestManager.getInstance().isTestRunning();

        if (testInfo != null) {
            testNameTextField.setText(testInfo.name);
            testIdTextField.setText(testInfo.id);
            testNameTextField.setEnabled(false);
            createNewButton.setEnabled(false);
            goToTestPageButton.setEnabled(true);
        } else {
            testNameTextField.setText("");
            testIdTextField.setText("");
            testNameTextField.setEnabled(!isRunning);
            createNewButton.setEnabled(!isRunning);
            goToTestPageButton.setEnabled(false);
        }
        testIdComboBox.setEnabled(!isRunning);
        reportNameTextField.setEnabled(!isRunning);
        reloadButton.setEnabled(!isRunning);
    }


    public String getReportName() {
        return reportNameTextField.getText();
    }

    public void setReportName(String reportName) {
        String newName = getValidReportName(reportName);
        if (!newName.equals(reportNameTextField.getText()))
            reportNameTextField.setText(newName);
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setAutoscrolls(true);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 18, new Insets(1, 1, 1, 1), -1, -1));
        panel1.setVisible(true);
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.windowBorder), "Info"));
        final JLabel label1 = new JLabel();
        label1.setText("User Key");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Test Info");
        panel1.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        signUpToBlazemeterButton = new JButton();
        signUpToBlazemeterButton.setActionCommand("Sign up to BlazeMeter!");
        signUpToBlazemeterButton.setLabel("Sign up to BlazeMeter's free-tier. It's immediate and no credit card is required.");
        signUpToBlazemeterButton.setText("Sign up to BlazeMeter's free-tier. It's immediate and no credit card is required.");
        signUpToBlazemeterButton.setToolTipText("Register/Login to BlazeMeter site and find your User Key on a profile page.");
        panel1.add(signUpToBlazemeterButton, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        goToTestPageButton = new JButton();
        goToTestPageButton.setActionCommand("Go to Test Page!");
        goToTestPageButton.setEnabled(true);
        goToTestPageButton.setHideActionText(false);
        goToTestPageButton.setLabel("Go to Test Page!");
        goToTestPageButton.setText("Go to Test Page!");
        goToTestPageButton.setToolTipText("Navigate to test page on Blazemeter site");
        goToTestPageButton.setVerifyInputWhenFocusTarget(false);
        goToTestPageButton.setVisible(true);
        panel1.add(goToTestPageButton, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Select Test");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        userKeyTextField = new JTextField();
        userKeyTextField.setText("*");
        userKeyTextField.setToolTipText("User key - can be found on your profile page , click \"?\" button for more info");
        panel2.add(userKeyTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        helpButton = new JButton();
        helpButton.setEnabled(true);
        helpButton.setFont(new Font("Tahoma", helpButton.getFont().getStyle(), 16));
        helpButton.setHideActionText(false);
        helpButton.setHorizontalAlignment(0);
        helpButton.setHorizontalTextPosition(0);
        helpButton.setIcon(new ImageIcon(getClass().getResource("/com/blazemeter/jmeter/common/question.png")));
        helpButton.setInheritsPopupMenu(true);
        helpButton.setMaximumSize(new Dimension(22, 22));
        helpButton.setMinimumSize(new Dimension(20, 20));
        helpButton.setPreferredSize(new Dimension(20, 20));
        helpButton.setText("");
        helpButton.setToolTipText("Help");
        panel2.add(helpButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        userInfoLabel = new JLabel();
        userInfoLabel.setText("");
        panel2.add(userInfoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testIdTextField = new JTextField();
        testIdTextField.setEditable(true);
        testIdTextField.setEnabled(false);
        testIdTextField.setToolTipText("Test id of current test");
        panel3.add(testIdTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), new Dimension(70, -1), new Dimension(70, -1), 0, false));
        testNameTextField = new JTextField();
        testNameTextField.setAutoscrolls(false);
        testNameTextField.setToolTipText("Test name of current test");
        panel3.add(testNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createNewButton = new JButton();
        createNewButton.setEnabled(true);
        createNewButton.setFont(new Font("Tahoma", createNewButton.getFont().getStyle(), 16));
        createNewButton.setHideActionText(false);
        createNewButton.setHorizontalAlignment(0);
        createNewButton.setHorizontalTextPosition(0);
        createNewButton.setIcon(new ImageIcon(getClass().getResource("/com/blazemeter/jmeter/common/plus.png")));
        createNewButton.setMaximumSize(new Dimension(22, 22));
        createNewButton.setMinimumSize(new Dimension(20, 20));
        createNewButton.setPreferredSize(new Dimension(20, 20));
        createNewButton.setText("");
        createNewButton.setToolTipText("Create new test");
        panel3.add(createNewButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testIdComboBox = new JComboBox();
        testIdComboBox.setDoubleBuffered(true);
        testIdComboBox.setEditable(true);
        testIdComboBox.setEnabled(true);
        testIdComboBox.setToolTipText("Enter test id  or select test from list , click refresh button if you want to load test list from the server.");
        panel4.add(testIdComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        reloadButton = new JButton();
        reloadButton.setActionCommand("");
        reloadButton.setEnabled(true);
        reloadButton.setFont(new Font("Tahoma", reloadButton.getFont().getStyle(), 16));
        reloadButton.setHorizontalTextPosition(0);
        reloadButton.setIcon(new ImageIcon(getClass().getResource("/com/blazemeter/jmeter/common/refresh.png")));
        reloadButton.setMaximumSize(new Dimension(20, 20));
        reloadButton.setMinimumSize(new Dimension(20, 20));
        reloadButton.setPreferredSize(new Dimension(20, 20));
        reloadButton.setText("");
        reloadButton.setToolTipText("Reload tests list from server");
        reloadButton.setVerticalAlignment(0);
        panel4.add(reloadButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Run Mode");
        panel1.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runLocal = new JRadioButton();
        runLocal.setSelected(true);
        runLocal.setText("Locally (Reporting Only)");
        panel5.add(runLocal, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        runRemote = new JRadioButton();
        runRemote.setSelected(false);
        runRemote.setText("Run in the Cloud");
        panel5.add(runRemote, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cloudPanel = new JPanel();
        cloudPanel.setLayout(new GridLayoutManager(4, 30, new Insets(1, 1, 1, 1), -1, -1));
        cloudPanel.setEnabled(true);
        cloudPanel.setVisible(true);
        mainPanel.add(cloudPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cloudPanel.setBorder(BorderFactory.createTitledBorder("Run in the Cloud Settings"));
        final JLabel label5 = new JLabel();
        label5.setRequestFocusEnabled(false);
        label5.setText("Users #");
        cloudPanel.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Location");
        cloudPanel.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel6, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        numberOfUserTextBox = new JTextField();
        numberOfUserTextBox.setEditable(true);
        numberOfUserTextBox.setEnabled(true);
        numberOfUserTextBox.setText("300");
        numberOfUserTextBox.setToolTipText("Number of users for testing in cloud");
        panel6.add(numberOfUserTextBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        numberOfUsersSlider = new JSlider();
        numberOfUsersSlider.setInverted(false);
        numberOfUsersSlider.setMajorTickSpacing(1000);
        numberOfUsersSlider.setMaximum(8400);
        numberOfUsersSlider.setMinimum(0);
        numberOfUsersSlider.setMinorTickSpacing(200);
        numberOfUsersSlider.setPaintLabels(true);
        numberOfUsersSlider.setPaintTicks(true);
        numberOfUsersSlider.setValue(100);
        numberOfUsersSlider.setValueIsAdjusting(false);
        panel6.add(numberOfUsersSlider, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        locationComboBox = new JComboBox();
        locationComboBox.setDoubleBuffered(true);
        locationComboBox.setEditable(false);
        locationComboBox.setEnabled(true);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("EU West (Ireland)");
        defaultComboBoxModel1.addElement("US East (Virginia)");
        defaultComboBoxModel1.addElement("US West (N.California)");
        defaultComboBoxModel1.addElement("US West (Oregon)");
        defaultComboBoxModel1.addElement("Asia Pacific (Singapore)");
        defaultComboBoxModel1.addElement("Japan (Tokyo)");
        defaultComboBoxModel1.addElement("South America (San Paulo)");
        locationComboBox.setModel(defaultComboBoxModel1);
        locationComboBox.setToolTipText("Select location");
        panel7.add(locationComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        cloudPanel.add(spacer4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        enginesDescription = new JTextField();
        enginesDescription.setEditable(false);
        enginesDescription.setEnabled(false);
        enginesDescription.setText("1 MEDIUM engine");
        cloudPanel.add(enginesDescription, new GridConstraints(0, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        overridesPanel = new JPanel();
        overridesPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(overridesPanel, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rampupSpinner = new JSpinner();
        rampupSpinner.setAutoscrolls(false);
        rampupSpinner.setFocusTraversalPolicyProvider(true);
        panel8.add(rampupSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
        final JLabel label7 = new JLabel();
        label7.setRequestFocusEnabled(false);
        label7.setText("Rampup Period (seconds)");
        panel8.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        iterationsSpinner = new JSpinner();
        panel9.add(iterationsSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
        final JLabel label8 = new JLabel();
        label8.setRequestFocusEnabled(false);
        label8.setText("# Iterations");
        panel9.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel10, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        durationSpinner = new JSpinner();
        panel10.add(durationSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
        final JLabel label9 = new JLabel();
        label9.setRequestFocusEnabled(false);
        label9.setText("Duration (minutes)");
        panel10.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        runInTheCloud = new JButton();
        runInTheCloud.setActionCommand("start");
        runInTheCloud.setFont(new Font(runInTheCloud.getFont().getName(), runInTheCloud.getFont().getStyle(), 16));
        runInTheCloud.setHideActionText(false);
        runInTheCloud.setInheritsPopupMenu(true);
        runInTheCloud.setLabel("Run in the Cloud!");
        runInTheCloud.setText("Run in the Cloud!");
        cloudPanel.add(runInTheCloud, new GridConstraints(1, 5, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 100), null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        cloudPanel.add(spacer5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final Spacer spacer6 = new Spacer();
        cloudPanel.add(spacer6, new GridConstraints(3, 12, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        addFilesButton = new JButton();
        addFilesButton.setActionCommand("Add Files for Cloud Test");
        addFilesButton.setLabel("Add Files for Cloud Test");
        addFilesButton.setText("Add Files for Cloud Test");
        cloudPanel.add(addFilesButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("* 0 means \"FOREVER\"");
        cloudPanel.add(label10, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("* 0 means \"Limited by Test Session Time\"");
        cloudPanel.add(label11, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final Spacer spacer7 = new Spacer();
        cloudPanel.add(spacer7, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(40, -1), null, 0, false));
        localPanel = new JPanel();
        localPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(localPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        localPanel.setBorder(BorderFactory.createTitledBorder("Local Run"));
        final Spacer spacer8 = new Spacer();
        localPanel.add(spacer8, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        reportNameTextField = new JTextField();
        reportNameTextField.setText("sample.jtl");
        reportNameTextField.setToolTipText("Report name that listener will create and use for uploading statistics to it.");
        localPanel.add(reportNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Report Name");
        localPanel.add(label12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        localPanel.add(spacer9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        infoPanel.setVisible(false);
        mainPanel.add(infoPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Run in the Cloud Settings"));
        final Spacer spacer10 = new Spacer();
        infoPanel.add(spacer10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final Spacer spacer11 = new Spacer();
        infoPanel.add(spacer11, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("");
        infoLabel.setVerifyInputWhenFocusTarget(false);
        infoPanel.add(infoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(userKeyTextField);
        label2.setLabelFor(testNameTextField);
        label3.setLabelFor(testIdComboBox);
        label4.setLabelFor(testNameTextField);
        label5.setLabelFor(testNameTextField);
        label6.setLabelFor(testIdComboBox);
        label7.setLabelFor(testNameTextField);
        label8.setLabelFor(testNameTextField);
        label9.setLabelFor(testNameTextField);
        label12.setLabelFor(reportNameTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(runRemote);
        buttonGroup.add(runLocal);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
