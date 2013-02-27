package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.testinfo.UserInfo;
import com.blazemeter.jmeter.utils.BlazemeterApi;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.TestStatus;
import com.blazemeter.jmeter.utils.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.Save;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/29/12
 * Time: 12:29
 */
public class TestPanelGui {
    private static final String NEW = "---NEW---";
    private static final String EMPTY = "";
    private static final String HELP_URL = "http://community.blazemeter.com/knowledgebase/articles/83191-blazemeter-plugin-to-jmeter#user_key";
    private static final String SELECT_TEST = "Please, select test from list";
    private static final String LOADING_TEST_INFO = "Loading test info, please wait";
    private static final String CAN_NOT_BE_RUN = "This test could not be run from Jmeter Plugin. Please, select another one from the list above.";
    private static final String TEST_INFO_IS_LOADED = "Test info is loaded";
    private static long lastCloudPanelUpdate = 0;
    private static boolean areListenersInitialized = false;
    private JTextField userKeyTextField;
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
    private JPanel overridesPanel;
    private JPanel infoPanel;
    private JLabel infoLabel;
    private JLabel userInfoLabel;
    private JButton addFilesButton;
    private JButton editJMXLocallyButton;


    public TestPanelGui() {

        createUIComponents();
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
                BmTestManager bmTestManager = BmTestManager.getInstance();
                String userKey = bmTestManager.getUserKey();
                if (userKey == null || userKey.isEmpty()) {
                    JMeterUtils.reportErrorToUser("Please enter user key", "No user key");
                    bmTestManager.setUserKeyValid(false);
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
                    BmTestManager.getInstance().setTestInfo(ti);
                    BmTestManager.getInstance().uploadJmx();
                }
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
                    if (userInfo.getMaxUsersLimit() > 8400 && userInfo.getMaxEnginesLimit() > 14) {
                        userInfo.setMaxUsersLimit(8400);
                        userInfo.setMaxEnginesLimit(14);
                    }
                    //configure numberOfUserSlider depending on UserInfo
                    userInfoLabel.setText(userInfo.toString());
                    numberOfUsersSlider.setMaximum(userInfo.getMaxUsersLimit());
                    numberOfUsersSlider.setMinimum(0);
                    numberOfUsersSlider.setMajorTickSpacing(userInfo.getMaxUsersLimit() / 4);
                    numberOfUsersSlider.setMinorTickSpacing(userInfo.getMaxUsersLimit() / 12);
                    Dictionary labels = numberOfUsersSlider.createStandardLabels(numberOfUsersSlider.getMajorTickSpacing());
                    numberOfUsersSlider.setLabelTable(labels);
                }
            }
        });

        signUpToBlazemeterButton.setEnabled(BmTestManager.getInstance().getUserKey() == null || BmTestManager.
                getInstance().
                getUserKey().
                isEmpty());

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

        editJMXLocallyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (BmTestManager.getInstance().getTestInfo().id.isEmpty()) {
                    JMeterUtils.reportErrorToUser("JMX can not be downloaded: test id is empty", "Empty test id");
                    return;
                }
                GuiPackage guiPackage = GuiPackage.getInstance();
                if (guiPackage.isDirty()) {
                    int chosenOption = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                            "Do you want to save changes in current test-plan?",
                            JMeterUtils.getResString("save?"),
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (chosenOption == JOptionPane.CANCEL_OPTION) {
                        return;
                    } else if (chosenOption == JOptionPane.YES_OPTION) {
                        Save save = new Save();
                        try {
                            save.doAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "save_as"));
                        } catch (IllegalUserActionException iuae) {
                            BmLog.error("Can not save file," + iuae);
                        }
                        downloadJMX();
                    } else if (chosenOption == JOptionPane.NO_OPTION) {
                        downloadJMX();
                    }
                } else {
                    downloadJMX();
                }

            }


            // save jmx on the disk in user.dir with the name.equals(filename=)
            // open jmx in JMeter for editing
            // edit jmx
            // wait until user clicksSaveButton and upload script back to server;

        });

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
                int engines;
                String engineSize;
                int usersPerEngine;

                ArrayList<String> enginesParameters = calculateEnginesForTest(numberOfUsers);
                engines = Integer.valueOf(enginesParameters.get(0));
                engineSize = enginesParameters.get(1).equals("m1.medium") ? "MEDIUM ENGINE" : "LARGE ENGINE";
                usersPerEngine = Integer.valueOf(enginesParameters.get(2));
                if (numberOfUsers <= 300) {
                    enginesDescription.setText(String.format("JMETER CONSOLE -  %d users", usersPerEngine));

                } else {
                    enginesDescription.setText(String.format("%d %s x %d users", engines, engineSize, usersPerEngine));

                }
                numberOfUserTextBox.setText(Integer.toString(usersPerEngine * engines));
            }
        });


        runInTheCloud.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogButton;
                BmTestManager bmTestManager = BmTestManager.getInstance();

                if ("start".equals(e.getActionCommand().toLowerCase())) {
                    if (bmTestManager.getUserKey().isEmpty()) {
                        JMeterUtils.reportErrorToUser("Please, set up user key.",
                                "User key is not set");
                        return;
                    }
                    dialogButton = JOptionPane.showConfirmDialog(mainPanel, "Are you sure that you want to start the test?",
                            "Start test?",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogButton == JOptionPane.YES_OPTION) {
                        startInTheCloud();
                        bmTestManager.NotifyTestInfoChanged();
                    }

                } else {
                    dialogButton = JOptionPane.showConfirmDialog(mainPanel, "Are you sure that you want to stop the test? ",
                            "Stop test?",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogButton == JOptionPane.YES_OPTION) {
                        bmTestManager.stopInTheCloud();
                        bmTestManager.NotifyTestInfoChanged();
                    }
                }
                updateCloudPanel(7000);
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
        BmTestManager bmTestManager = BmTestManager.getInstance();

        int id = bmTestManager.runInTheCloud();
        if (id != -1) {

            String url = bmTestManager.getTestUrl();
            if (url != null)
                url = url.substring(0, url.length() - 5);
            Utils.Navigate(url);
        }

        TestInfo testInfo = bmTestManager.getTestInfo();
        if (testInfo == null) {
            return;
        }
        testInfo = BlazemeterApi.getInstance().getTestRunStatus(BmTestManager.getInstance().getUserKey(),
                bmTestManager.getTestInfo().id, true);
        configureMainPanelControls(testInfo);
        bmTestManager.setTestInfo(testInfo);
        bmTestManager.NotifyTestInfoChanged();
    }

    public void downloadJMX() {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        BlazemeterApi blazemeterApi = BlazemeterApi.getInstance();
        TestInfo testInfo = bmTestManager.getTestInfo();
        File file = blazemeterApi.downloadJmx(bmTestManager.getUserKey(), testInfo.id);
        Utils.openJMX(file);
    }

    private void enableCloudControls(boolean isEnabled) {
        locationComboBox.setEnabled(isEnabled);
        numberOfUsersSlider.setEnabled(isEnabled);
        numberOfUserTextBox.setEnabled(isEnabled);
        rampupSpinner.setEnabled(isEnabled);
        iterationsSpinner.setEnabled(isEnabled);
        durationSpinner.setEnabled(isEnabled);
        addFilesButton.setEnabled(isEnabled);
        editJMXLocallyButton.setEnabled(isEnabled);
    }

    private void enableMainPanelControls(boolean isEnabled) {
        testIdTextField.setEnabled(isEnabled);
        testIdComboBox.setEnabled(isEnabled);
        testNameTextField.setEnabled(isEnabled);
        helpButton.setEnabled(isEnabled);
        signUpToBlazemeterButton.setEnabled(isEnabled);
        reloadButton.setEnabled(isEnabled);
        createNewButton.setEnabled(isEnabled);
        goToTestPageButton.setEnabled(isEnabled);
        runLocal.setEnabled(isEnabled);
        runRemote.setEnabled(isEnabled);
    }

    private void resetCloudPanel() {
        numberOfUsersSlider.setValue(0);
        numberOfUserTextBox.setText("0");
        rampupSpinner.setValue(0);
        iterationsSpinner.setValue(0);
        durationSpinner.setValue(0);
        runInTheCloud.setEnabled(false);
        addFilesButton.setEnabled(false);
    }

    private ArrayList<String> calculateEnginesForTest(int numberOfUsers) {
        ArrayList<String> enginesParameters = new ArrayList<String>(3);
        int engines = 0;
        String engineSize = "m1.medium";
        int userPerEngine = 0;

        UserInfo userInfo = BmTestManager.getInstance().getUserInfo();


        if (numberOfUsers <= 300) {
            userPerEngine = numberOfUsers;
        } else {
            engines = numberOfUsers / 300;
            if (engines < userInfo.getMaxEnginesLimit()) {
                if (numberOfUsers % 300 > 0) {
                    engines++;
                }
            } else {
                engineSize = "m1.large";
                engines = numberOfUsers / 600;
                if (numberOfUsers % 600 > 0) {
                    engines++;
                }
            }
            userPerEngine = numberOfUsers / engines;
        }

        enginesParameters.add(String.valueOf(engines));
        enginesParameters.add(engineSize);
        enginesParameters.add(String.valueOf(userPerEngine));
        return enginesParameters;


    }


    private void saveCloudTest() {
        UserInfo userInfo = BmTestManager.getInstance().getUserInfo();

        int numberOfUsers = numberOfUsersSlider.getValue();

        ArrayList<String> enginesParameters = calculateEnginesForTest(numberOfUsers);


        int engines = Integer.valueOf(enginesParameters.get(0));
        String engineSize = enginesParameters.get(1);
        int userPerEngine = Integer.valueOf(enginesParameters.get(2));


        int iterations = Integer.parseInt(iterationsSpinner.getValue().toString());
        iterations = iterations > 0 || iterations < 1001 ? iterations : -1;

        int rumpUp = Integer.parseInt(rampupSpinner.getValue().toString());
        int duration = Integer.parseInt(durationSpinner.getValue().toString());
        duration = duration > 0 ? duration : -1;
        String location = locationComboBox.getSelectedItem().toString();
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        if (testInfo != null) {
            BlazemeterApi.getInstance().updateTestSettings(BmTestManager.getInstance().getUserKey(),
                    BmTestManager.getInstance().getTestInfo().id,
                    location, engines, engineSize, userPerEngine, iterations, rumpUp, duration);
        } else {
            JMeterUtils.reportErrorToUser("Please, select test", "Test is not selected");
        }
    }

    private void clearTestInfo() {
        testIdComboBox.removeAllItems();
        BmTestManager.getInstance().setTestInfo(null);
    }

    /**
     * Here some heavy GUI listeners are initialized;
     */
    public void initializeListeners() {
        BmTestManager bmTestManager = BmTestManager.getInstance();

        if (!areListenersInitialized) {
            areListenersInitialized = true;
            testIdComboBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    BmTestManager bmTestManager = BmTestManager.getInstance();
                    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                        Object selectedTest = testIdComboBox.getSelectedItem();
                        if (selectedTest instanceof TestInfo) {
                            TestInfo testInfo = (TestInfo) selectedTest;
                            if (testInfo.name != NEW & !testInfo.name.isEmpty()) {
                                bmTestManager.setTestInfo(testInfo);
                                bmTestManager.NotifyTestInfoChanged();
                            }
                        } else if (Utils.isInteger(selectedTest.toString())) {
                            TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(bmTestManager.getUserKey(),
                                    selectedTest.toString(), true);
                            BmLog.console(ti.toString());
                            if (ti.status == TestStatus.Running || ti.status == TestStatus.NotRunning) {
                                bmTestManager.setTestInfo(ti);
                                setTestInfo(ti);
                                bmTestManager.NotifyTestInfoChanged();
                            } else {
                                JMeterUtils.reportErrorToUser(ti.error, "Test not found error");
                                bmTestManager.setTestInfo(null);
                                bmTestManager.NotifyTestInfoChanged();
                            }
                        } else if (selectedTest.toString().equals(NEW)) {
                            testIdComboBox.setSelectedItem(NEW);
                            configureMainPanelControls(null);
                            resetCloudPanel();
                            enableCloudControls(false);
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
                        BmTestManager.getInstance().getUserInfo();
                    }
                }).start();
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
                            BmTestManager bmTestManager = BmTestManager.getInstance();
                            bmTestManager.setUserKey(newVal);
                            if (!newVal.isEmpty())
                                fetchUserTestsAsync();
                        }
                    }
                });
            }
            //Here should be all changes of TestInfo processed
            bmTestManager.testInfoNotificationListeners.add(new BmTestManager.TestInfoNotification() {
                @Override
                public void onTestInfoChanged(TestInfo testInfo) {
                    if (testInfo == null) {
                        return;
                    }
                    if (testIdComboBox.getItemCount() == 1) {
                        addTestId(testInfo, true);
                    }
                    if (testInfo.status == TestStatus.Running) {
                        runInTheCloud.setEnabled(true);
                        addFilesButton.setEnabled(false);
                        enableCloudControls(false);
                        runLocal.setEnabled(false);
                        runRemote.setEnabled(false);
                    }

                    if ((testInfo.status == TestStatus.NotRunning)) {
                        if (BmTestManager.getInstance().getIsLocalRunMode() & (BmTestManager.isTestRunning())) {
                            StandardJMeterEngine.stopEngine();
                        }
                        runInTheCloud.setEnabled(true);
                        addFilesButton.setEnabled(true);
                        enableCloudControls(true);
                        runLocal.setEnabled(true);
                        runRemote.setEnabled(true);
                        configureMainPanelControls(testInfo);

                    }


                    setTestInfo(testInfo);
                    if ((testInfo != null) & (testInfo.name != NEW) & (!testInfo.name.isEmpty()) &
                            (testInfoChecker == null || testInfoChecker.isInterrupted())) {
                        startTestInfoChecker();
                    }
                    if (testInfo.name == NEW || (testInfo.name.isEmpty())) {
                        stopTestInfoChecker();
                    }
                }
            });
            //Processing serverStatusChangedNotification
            bmTestManager.serverStatusChangedNotificationListeners.add(new BmTestManager.ServerStatusChangedNotification() {
                @Override
                public void onServerStatusChanged() {
                    BmTestManager.ServerStatus serverStatus = BmTestManager.getServerStatus();
                    switch (serverStatus) {
                        case AVAILABLE:
                            TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
                            if (testInfo.status == TestStatus.Running) {
                                startTestInfoChecker();
                            } else {
                                enableMainPanelControls(true);
                            }
                            break;
                        case NOT_AVAILABLE:
                            enableMainPanelControls(false);
                            enableCloudControls(false);
                            runInTheCloud.setEnabled(false);
                            stopTestInfoChecker();
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
        BlazemeterApi.getInstance().getTestsAsync(userKey, new BlazemeterApi.TestContainerNotifier() {
            @Override
            public void testReceived(ArrayList<TestInfo> tests) {
                testIdComboBox.removeAllItems();
                testIdComboBox.setEnabled(true);
                testIdComboBox.addItem(EMPTY);
                testIdComboBox.setSelectedItem(EMPTY);
                if (tests != null) {
                    testIdComboBox.removeAllItems();
                    testIdComboBox.addItem(NEW);
                    testIdComboBox.setSelectedItem(NEW);
                    BmTestManager.getInstance().setUserKeyValid(true);
                    for (TestInfo ti : tests) {
                        addTestId(ti, false);
                    }

                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Please enter valid user key", "Invalid user key", JOptionPane.ERROR_MESSAGE);
                    BmTestManager.getInstance().setUserKeyValid(false);
                    resetCloudPanel();
                    enableCloudControls(false);
                    testIdComboBox.setSelectedItem(EMPTY);
                }
            }
        });
    }

    public void setUserKey(String key) {
        userKeyTextField.setText(key);
        BmLog.debug("Setting user key" + key);
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


    private void runModeChanged(boolean isLocalRun) {
        if (isLocalRun) {
            cloudPanel.setVisible(false);
            infoPanel.setVisible(false);
        } else {
            cloudPanel.setVisible(true);
            infoPanel.setVisible(true);
        }
    }

    protected void setTestInfo(TestInfo testInfo) {
        if (testInfo == null || testInfo.isEmpty() || !testInfo.isValid()) {
            testInfo = new TestInfo();
            testInfo.name = NEW;
            testIdComboBox.setSelectedItem(testInfo.name);
            infoLabel.setText(SELECT_TEST);
            configureMainPanelControls(null);
        } else {
            testIdComboBox.setSelectedItem(testInfo);
            configureMainPanelControls(testInfo);
            infoLabel.setText(LOADING_TEST_INFO);
            runModeChanged(BmTestManager.getInstance().getIsLocalRunMode());
            infoLabel.setText(TEST_INFO_IS_LOADED);
        }
    }

    protected TestInfo getTestInfo() {
        TestInfo testInfo = new TestInfo();
        testInfo.id = testIdTextField.getText();
        testInfo.name = testNameTextField.getText();
        testInfo.status = runInTheCloud.getText().equals("Run in the Cloud!") ? TestStatus.NotRunning : TestStatus.Running;
        testInfo.error = null;
        testInfo.numberOfUsers = numberOfUsersSlider.getValue();
        testInfo.location = locationComboBox.getSelectedItem().toString();
        testInfo.overrides = null;
        testInfo.type = "jmeter";
        return testInfo;
    }

    private Thread updateCloudPanelThread;

    private void updateCloudPanel(int lastCloudUpdatePeriod) {
        long now = new Date().getTime();
        if (lastCloudPanelUpdate + lastCloudUpdatePeriod > now) {
            return;
        }
        lastCloudPanelUpdate = now;
        BmTestManager bmTestManager = BmTestManager.getInstance();
        if (bmTestManager.getUserKey().isEmpty() || !bmTestManager.isUserKeyValid()) {
            return;

        }

        interruptCloudPanelUpdate();
        updateCloudPanelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BmTestManager bmTestManager = BmTestManager.getInstance();
                TestInfo testInfo = bmTestManager.getTestInfo();
                String testId = "";
                if (testInfo == null) {
                    testInfo = new TestInfo();
                    testId = testIdComboBox.getSelectedItem().toString().split(" ")[0];
                    testInfo.id = testId;
                    bmTestManager.setTestInfo(testInfo);
                }
                testInfo = BlazemeterApi.getInstance().getTestRunStatus(bmTestManager.getUserKey(),
                        testInfo.id, true);

                bmTestManager.setTestInfo(testInfo);
                bmTestManager.NotifyTestInfoChanged();

                if (Thread.currentThread().isInterrupted())
                    return;
                if ("jmeter".equals(testInfo.type)) {
                    locationComboBox.setSelectedItem(testInfo.getLocation());
                    numberOfUsersSlider.setValue(testInfo.getNumberOfUsers());
                    if (testInfo.overrides != null) {
                        rampupSpinner.setValue(testInfo.overrides.rampup);
                        iterationsSpinner.setValue(testInfo.overrides.iterations == -1 ? 0 : testInfo.overrides.iterations);
                        durationSpinner.setValue(testInfo.overrides.duration == -1 ? 0 : testInfo.overrides.duration);
                    } else {
                        rampupSpinner.setValue(0);
                        iterationsSpinner.setValue(0);
                        durationSpinner.setValue(0);
                    }
                    runInTheCloud.setActionCommand(testInfo.status == TestStatus.Running ? "stop" : "start");
                    runInTheCloud.setText(testInfo.status == TestStatus.Running ? "Stop" : "Run in the Cloud!");
                } else {
                    infoLabel.setText(testIdComboBox.getSelectedItem().equals(NEW) ? SELECT_TEST : CAN_NOT_BE_RUN);
                }
                configureMainPanelControls(testInfo);
            }
        });
        updateCloudPanelThread.start();
    }

    private void interruptCloudPanelUpdate() {
        if (updateCloudPanelThread != null) {

            if (updateCloudPanelThread.isAlive()) {
                updateCloudPanelThread.interrupt();
                BmLog.debug("UpdatingCloudPanelThread is interrupted!");
            }
        }
    }

    private Thread testInfoChecker;


    private void startTestInfoChecker() {
        if (testInfoChecker == null) {
            testInfoChecker = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        updateCloudPanel(0);
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            BmLog.debug("TestStatusChecker was interrupted during sleeping");
                            return;
                        } finally {
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                        }

                    }
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                }
            }
            );
        }
        testInfoChecker.start();
    }

    private void stopTestInfoChecker() {
        if (testInfoChecker != null) {
            if (testInfoChecker.isAlive()) {
                testInfoChecker.interrupt();
                testInfoChecker = null;
                System.gc();
                BmLog.debug("TestStatusChecker is interrupted!");
            }
        }
    }

    private void configureMainPanelControls(TestInfo testInfo) {
        boolean isRunning = (testInfo != null && testInfo.status == TestStatus.Running);

        if (testInfo != null) {
            testNameTextField.setText(testInfo.name);
            testIdTextField.setText(testInfo.id);
            testNameTextField.setEnabled(!isRunning);
            createNewButton.setEnabled(!isRunning);
            goToTestPageButton.setEnabled(true);
        } else {
            testNameTextField.setText("");
            testIdTextField.setText("");
            testNameTextField.setEnabled(!isRunning);
            createNewButton.setEnabled(!isRunning);
            goToTestPageButton.setEnabled(false);
        }
        if (!BmTestManager.getInstance().isUserKeyFromProp()) {
            userKeyTextField.setEnabled(!isRunning);
        }
        testIdComboBox.setEnabled(!isRunning);
        testIdTextField.setEnabled(!isRunning);
        reloadButton.setEnabled(!isRunning);
    }

    private void createUIComponents() {
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("EU West (Ireland)");
        defaultComboBoxModel1.addElement("US East (Virginia)");
        defaultComboBoxModel1.addElement("US West (N.California)");
        defaultComboBoxModel1.addElement("US West (Oregon)");
        defaultComboBoxModel1.addElement("Asia Pacific (Singapore)");
        defaultComboBoxModel1.addElement("Japan (Tokyo)");
        defaultComboBoxModel1.addElement("South America (Sao Paulo)");
        defaultComboBoxModel1.addElement("Australia (Sydney)");
        locationComboBox.setModel(defaultComboBoxModel1);
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        mainPanel.setLayout(new GridLayoutManager(3, 6, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setAutoscrolls(true);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 18, new Insets(1, 1, 1, 1), -1, -1));
        panel1.setVisible(true);
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder("Info"));
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
        panel2.add(helpButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        userInfoLabel = new JLabel();
        userInfoLabel.setText("");
        panel2.add(userInfoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userKeyTextField = new JTextField();
        userKeyTextField.setText("Enter your user key");
        userKeyTextField.setToolTipText("User key - can be found on your profile page , click \"?\" button for more info");
        panel2.add(userKeyTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        createNewButton.setIcon(new ImageIcon(getClass().getResource("/com/blazemeter/jmeter/images/plus.png")));
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
        testIdComboBox.setEditable(false);
        testIdComboBox.setEnabled(true);
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
        runLocal.setSelected(false);
        runLocal.setText("Locally (Reporting Only)");
        panel5.add(runLocal, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        runRemote = new JRadioButton();
        runRemote.setSelected(true);
        runRemote.setText("Run in the Cloud");
        panel5.add(runRemote, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cloudPanel = new JPanel();
        cloudPanel.setLayout(new GridLayoutManager(5, 30, new Insets(1, 1, 1, 1), -1, -1));
        cloudPanel.setEnabled(true);
        cloudPanel.setVisible(true);
        mainPanel.add(cloudPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cloudPanel.setBorder(BorderFactory.createTitledBorder("Run in the Cloud Settings"));
        final JLabel label5 = new JLabel();
        label5.setRequestFocusEnabled(false);
        label5.setText("Users #");
        cloudPanel.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Location");
        cloudPanel.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel6, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        numberOfUserTextBox = new JTextField();
        numberOfUserTextBox.setEditable(true);
        numberOfUserTextBox.setEnabled(true);
        numberOfUserTextBox.setText("0");
        numberOfUserTextBox.setToolTipText("Number of users for testing in cloud");
        panel6.add(numberOfUserTextBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        numberOfUsersSlider = new JSlider();
        numberOfUsersSlider.setInverted(false);
        numberOfUsersSlider.setMajorTickSpacing(2100);
        numberOfUsersSlider.setMaximum(8400);
        numberOfUsersSlider.setMinimum(0);
        numberOfUsersSlider.setMinorTickSpacing(700);
        numberOfUsersSlider.setPaintLabels(true);
        numberOfUsersSlider.setPaintTicks(true);
        numberOfUsersSlider.setPaintTrack(true);
        numberOfUsersSlider.setRequestFocusEnabled(true);
        numberOfUsersSlider.setSnapToTicks(false);
        numberOfUsersSlider.setValue(0);
        numberOfUsersSlider.setValueIsAdjusting(true);
        panel6.add(numberOfUsersSlider, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        locationComboBox = new JComboBox();
        locationComboBox.setDoubleBuffered(true);
        locationComboBox.setEditable(false);
        locationComboBox.setEnabled(true);
        locationComboBox.setToolTipText("Select location");
        panel7.add(locationComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        cloudPanel.add(spacer4, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(180, -1), null, 0, false));
        enginesDescription = new JTextField();
        enginesDescription.setEditable(false);
        enginesDescription.setEnabled(false);
        enginesDescription.setText("JMETER CONSOLE");
        cloudPanel.add(enginesDescription, new GridConstraints(0, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        overridesPanel = new JPanel();
        overridesPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(overridesPanel, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
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
        panel8.add(panel9, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        iterationsSpinner = new JSpinner();
        panel9.add(iterationsSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
        final JLabel label8 = new JLabel();
        label8.setRequestFocusEnabled(false);
        label8.setText("# Iterations");
        panel9.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel10, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        durationSpinner = new JSpinner();
        panel10.add(durationSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
        final JLabel label9 = new JLabel();
        label9.setRequestFocusEnabled(false);
        label9.setText("Duration (minutes)");
        panel10.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        runInTheCloud = new JButton();
        runInTheCloud.setActionCommand("start");
        runInTheCloud.setEnabled(false);
        runInTheCloud.setFont(new Font(runInTheCloud.getFont().getName(), runInTheCloud.getFont().getStyle(), 16));
        runInTheCloud.setHideActionText(false);
        runInTheCloud.setInheritsPopupMenu(true);
        runInTheCloud.setLabel("Run in the Cloud!");
        runInTheCloud.setText("Run in the Cloud!");
        cloudPanel.add(runInTheCloud, new GridConstraints(2, 5, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 100), null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        cloudPanel.add(spacer5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final Spacer spacer6 = new Spacer();
        cloudPanel.add(spacer6, new GridConstraints(4, 12, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        addFilesButton = new JButton();
        addFilesButton.setActionCommand("Add Files for Cloud Test");
        addFilesButton.setEnabled(false);
        addFilesButton.setLabel("Add Files for Cloud Test");
        addFilesButton.setText("Add Files for Cloud Test");
        cloudPanel.add(addFilesButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("* 0 means \"FOREVER\"");
        cloudPanel.add(label10, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("* 0 means \"Limited by Test Session Time\"");
        cloudPanel.add(label11, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        editJMXLocallyButton = new JButton();
        editJMXLocallyButton.setText("Edit JMX locally");
        cloudPanel.add(editJMXLocallyButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        infoPanel.setVisible(true);
        mainPanel.add(infoPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Blazemeter System Messages:"));
        final Spacer spacer7 = new Spacer();
        infoPanel.add(spacer7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final Spacer spacer8 = new Spacer();
        infoPanel.add(spacer8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
