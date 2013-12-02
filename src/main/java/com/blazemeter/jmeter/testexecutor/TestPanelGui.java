package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.testexecutor.listeners.EditJMXLocallyButtonListener;
import com.blazemeter.jmeter.testexecutor.listeners.SaveUploadButtonListener;
import com.blazemeter.jmeter.testexecutor.notifications.IRunModeChangedNotification;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;
import com.blazemeter.jmeter.testexecutor.notifications.ITestUserKeyNotification;
import com.blazemeter.jmeter.testexecutor.notifications.IUserInfoChangedNotification;
import com.blazemeter.jmeter.testexecutor.panels.JMeterPropertyPanel;
import com.blazemeter.jmeter.testinfo.*;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/29/12
 * Time: 12:29
 */
public class TestPanelGui {
    private static TestPanelGui gui;

    //Gui controls
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
    private JLabel userInfoLabel;
    private JButton addFilesButton;
    private JButton editJMXLocallyButton;
    private JButton saveUploadButton;
    private JPanel jMeterPropertyPanel = new JMeterPropertyPanel();


    public TestPanelGui() {

        $$$setupUI$$$();
        configureUIComponents();
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
                String testName = testNameTextField.getText().trim();
                if (testName.isEmpty() | testName.equals(Constants.NEW)) {
                    testName = JOptionPane.showInputDialog(mainPanel, "Please enter valid test name!");
                    if (testName == null || testName.trim().isEmpty())
                        return;
                }
                if (Utils.isTestPlanEmpty()) {
                    JMeterUtils.reportErrorToUser("Test-plan should contain at least one Thread Group");
                    return;
                }
                int numberOfUsers = numberOfUsersSlider.getValue();
                TestInfo ti = bmTestManager.createTest(userKey, testName);
                ti.setLocation((String) locationComboBox.getSelectedItem());
                ti.setNumberOfUsers(numberOfUsers != 0 ? numberOfUsers : 1);
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


        locationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                String locationId = Utils.getLocationId((String) locationComboBox.getSelectedItem());
                if (!locationId.isEmpty()) {
                    BmTestManager.getInstance().getTestInfo().setLocation(locationId);
                }
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

        editJMXLocallyButton.addActionListener(new EditJMXLocallyButtonListener());

        numberOfUserTextBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                int numberOfUsers = 0;
                try {
                    numberOfUsers = Integer.valueOf(numberOfUserTextBox.getText().trim());
                    if (numberOfUsers == 0) {
                        numberOfUserTextBox.setText("1");
                    }
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
                TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
                testInfo.setNumberOfUsers(numberOfUsers);
                ArrayList<String> enginesParameters = calculateEnginesForTest(numberOfUsers);
                engines = Integer.valueOf(enginesParameters.get(0));
                engineSize = enginesParameters.get(1).equals("m1.medium") ? "MEDIUM ENGINE" : "LARGE ENGINE";
                usersPerEngine = Integer.valueOf(enginesParameters.get(2));
                if (numberOfUsers <= 300) {
                    enginesDescription.setText(String.format("JMETER CONSOLE -  %d users", usersPerEngine));
                    numberOfUserTextBox.setText(Integer.toString(numberOfUsers));
                } else {
                    enginesDescription.setText(String.format("%d %s x %d users", engines, engineSize, usersPerEngine));
                    numberOfUserTextBox.setText(Integer.toString(usersPerEngine * engines));
                }
            }
        });
        runInTheCloud.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogButton;
                BmTestManager bmTestManager = BmTestManager.getInstance();
                if ("start".equals(e.getActionCommand().toLowerCase())) {
                    if (bmTestManager.getUserKey().isEmpty()) {
                        JMeterUtils.reportErrorToUser("Please, set up user key.", "User key is not set.");
                        return;
                    }
                    dialogButton = JOptionPane.showConfirmDialog(mainPanel, "Are you sure that you want to start the test?",
                            "Start test?",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogButton == JOptionPane.YES_OPTION) {
                        startInTheCloud();

                 /*
                   OperationProgressDialog operationProgressDialog = new OperationProgressDialog("Please, wait...",
                                "Operation will take a few seconds to execute. Your patience is appreciated.");
                        operationProgressDialog.windowOpened(new WindowEvent(operationProgressDialog,WindowEvent.WINDOW_OPENED));
                        operationProgressDialog.windowClosing(new WindowEvent(operationProgressDialog,WindowEvent.WINDOW_CLOSING));
                 */

                    }

                } else {
                    dialogButton = JOptionPane.showConfirmDialog(mainPanel, "Are you sure that you want to stop the test? ",
                            "Stop test?",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogButton == JOptionPane.YES_OPTION) {
                        bmTestManager.stopInTheCloud();
                    }
                }
            }
        });


        rampupSpinner.setModel(new SpinnerNumberModel(0, 0, 3600, 60));
        rampupSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    BmTestManager.getInstance().getTestInfo().getOverrides().setRampup((Integer) rampupSpinner.getValue());
                } catch (NullPointerException npe) {
                    BmLog.error("RampUpSpinner was not activated yet. Try again later");
                }
            }
        });
        iterationsSpinner.setModel(new SpinnerNumberModel(0, 0, 1010, 1));
        iterationsSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    BmTestManager.getInstance().getTestInfo().getOverrides().setIterations((Integer) iterationsSpinner.getValue());

                } catch (NullPointerException npe) {
                    BmLog.error("IterationsSpinner was not activated yet. Try again later");
                }
            }
        });
        durationSpinner.setModel(new SpinnerNumberModel(0, 0, 480, 60));
        durationSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    BmTestManager.getInstance().getTestInfo().getOverrides().setDuration((Integer) durationSpinner.getValue());
                } catch (NullPointerException npe) {
                    BmLog.error("DurationSpinner was not activated yet. Try again later");
                }
            }
        });

        addFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String url = BmTestManager.getInstance().getTestUrl() + "/files";
                if (url != null)
                    Utils.Navigate(url);
            }
        });

        saveUploadButton.addActionListener(new SaveUploadButtonListener());
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void startInTheCloud() {
        saveCloudTest();
        BmTestManager bmTestManager = BmTestManager.getInstance();
        TestInfoController.stop();
        bmTestManager.runInTheCloud();
        TestInfo testInfo = bmTestManager.getTestInfo();
        if (testInfo.getError() == null & testInfo.getStatus() == TestStatus.Running) {
            String url = bmTestManager.getTestUrl();
            if (url != null)
                url = url.substring(0, url.length() - 5);
            Utils.Navigate(url);
        }
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
        saveUploadButton.setEnabled(isEnabled);
    }

    private void enableMainPanelControls(boolean isEnabled) {
//        testIdTextField.setEnabled(isEnabled);
        testIdComboBox.setEnabled(isEnabled);
//        testNameTextField.setEnabled(isEnabled);
        reloadButton.setEnabled(isEnabled);
        createNewButton.setEnabled(isEnabled);
        goToTestPageButton.setEnabled(isEnabled);
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
        BmTestManager bmTestManager = BmTestManager.getInstance();
        int numberOfUsers = numberOfUsersSlider.getValue();

        ArrayList<String> enginesParameters = calculateEnginesForTest(numberOfUsers);
        int userPerEngine = Integer.valueOf(enginesParameters.get(2));

        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        if (testInfo != null) {
            if (userPerEngine == 0) {
                JMeterUtils.reportErrorToUser("Can't set up test with 0 users. " +
                        " '1' will be saved");
                userPerEngine = 1;
                Overrides overrides = testInfo.getOverrides();
                if (overrides != null) {
                    testInfo.getOverrides().setThreads(userPerEngine);
                } else {
                    overrides = new Overrides((Integer) durationSpinner.getValue(),
                            (Integer) iterationsSpinner.getValue(),
                            (Integer) rampupSpinner.getValue(),
                            userPerEngine);
                    testInfo.setOverrides(overrides);
                }
                testInfo.setNumberOfUsers(Integer.valueOf(userPerEngine));
            }
            /*
            BPC-207
             */
            JMeterPropertyPanel propertyPanel = (JMeterPropertyPanel) jMeterPropertyPanel;
            Properties jmeterProperties = propertyPanel.getData();
            testInfo.setJmeterProperties(jmeterProperties);
            /*
            BPC-207
             */
            testInfo = bmTestManager.updateTestSettings(bmTestManager.getUserKey(),
                    bmTestManager.getTestInfo());
            bmTestManager.setTestInfo(testInfo);

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
    public void initListeners() {
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
                        //configure numberOfUserSlider depending on UserInfo
                        numberOfUsersSlider.setMinimum(0);
                        userInfoLabel.setText(userInfo.toString());
                        numberOfUsersSlider.setMaximum(userInfo.getMaxUsersLimit());
                        numberOfUsersSlider.setMajorTickSpacing(userInfo.getMaxUsersLimit() / 4);
                        numberOfUsersSlider.setMinorTickSpacing(userInfo.getMaxUsersLimit() / 12);
                        Dictionary labels = numberOfUsersSlider.createStandardLabels(numberOfUsersSlider.getMajorTickSpacing());
                        numberOfUsersSlider.setLabelTable(labels);

                        //set locations list
                        JSONArray locations = userInfo.getLocations();
                        if (locations.length() > 0) {
                            locationComboBox.removeAllItems();
                            try {
                                for (int i = 0; i < locations.length(); ++i) {
                                    JSONObject location = locations.getJSONObject(i);
                                    locationComboBox.addItem(location.get("title"));
                                }
                            } catch (JSONException je) {
                                BmLog.error("Error during parsing locations JSONArray: " + je.getMessage());
                            }
                        }
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
                            resetCloudPanel();
                            enableCloudControls(false);
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
                        runInTheCloud.setEnabled(true);
                        addFilesButton.setEnabled(false);
                        enableCloudControls(false);
                        runLocal.setEnabled(false);
                        runRemote.setEnabled(false);
                        Utils.enableElements(jMeterPropertyPanel, false);
                    }

                    if ((testInfo.getStatus() == TestStatus.NotRunning)) {
                        Utils.enableElements(jMeterPropertyPanel, true);
                        boolean isTestIdEmpty = testInfo.getId().isEmpty();
                        runInTheCloud.setEnabled(!isTestIdEmpty);
                        addFilesButton.setEnabled(!isTestIdEmpty);
                        enableCloudControls(!isTestIdEmpty);

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
                            enableCloudControls(!testIsRunning);
                            runInTheCloud.setEnabled(!testIsRunning);
                            Utils.enableElements(jMeterPropertyPanel, !testIsRunning);
                            break;
                        case NOT_AVAILABLE:
                            enableMainPanelControls(false);
                            enableCloudControls(false);
                            runInTheCloud.setEnabled(false);
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
        BlazemeterApi.getInstance().getTestsAsync(userKey, new BlazemeterApi.TestContainerNotifier() {
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
                    if (curTest.length > 0) {
                        curTestId = curTest[0];
                        curTestName = curTest[1];

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
                    resetCloudPanel();
                    enableCloudControls(false);
                    testIdComboBox.setSelectedItem(Constants.EMPTY);
                }
            }
        });
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
            if ("jmeter".equals(testInfo.getType())) {
                String locationTitle = Utils.getLocationTitle(testInfo.getLocation());
                if (!locationTitle.isEmpty()) {
                    locationComboBox.setSelectedItem(locationTitle);
                }
                numberOfUsersSlider.setValue(testInfo.getNumberOfUsers());
                if (testInfo.getOverrides() != null) {
                    rampupSpinner.setValue(testInfo.getOverrides().getRampup());
                    iterationsSpinner.setValue(testInfo.getOverrides().getIterations() == -1 ? 0 : testInfo.getOverrides().getIterations());
                    durationSpinner.setValue(testInfo.getOverrides().getDuration() == -1 ? 0 : testInfo.getOverrides().getDuration());
                } else {
                    rampupSpinner.setValue(0);
                    iterationsSpinner.setValue(0);
                    durationSpinner.setValue(0);
                }
                runInTheCloud.setActionCommand(testInfo.getStatus() == TestStatus.Running ? "stop" : "start");
                runInTheCloud.setText(testInfo.getStatus() == TestStatus.Running ? "Stop" : "Run in the Cloud!");
            }
        }
    }


    private void configureMainPanelControls(TestInfo testInfo) {
        boolean isRunning = (testInfo != null && testInfo.getStatus() == TestStatus.Running);

        if (testInfo != null) {
            testNameTextField.setText(testInfo.getName());
            testIdTextField.setText(testInfo.getId());
            testNameTextField.setEnabled(!isRunning);
            createNewButton.setEnabled(!isRunning);
            goToTestPageButton.setEnabled(!testInfo.getId().isEmpty());
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
        testIdComboBox.setEnabled(!isRunning & testIdComboBox.getItemCount() > 0);
//        testIdTextField.setEnabled(!isRunning);
        reloadButton.setEnabled(!isRunning);
    }

    public static TestPanelGui getGui() {
        if (gui == null) {
            gui = new TestPanelGui();
        }
        return gui;
    }

    private void configureUIComponents() {
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("EU West (Ireland)");
        defaultComboBoxModel1.addElement("US East (Virginia)");
        defaultComboBoxModel1.addElement("US West (N.California)");
        defaultComboBoxModel1.addElement("US West (Oregon)");
        defaultComboBoxModel1.addElement("Asia Pacific (Singapore)");
        defaultComboBoxModel1.addElement("Japan (Tokyo)");
        defaultComboBoxModel1.addElement("South America (San Paulo)");
        defaultComboBoxModel1.addElement("Australia (Sydney)");
        locationComboBox.setModel(defaultComboBoxModel1);
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
        createUIComponents();
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
        userInfoLabel.setText("");
        panel2.add(userInfoLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userKeyTextField = new JTextField();
        userKeyTextField.setText("Enter your user key");
        userKeyTextField.setToolTipText("User key - can be found on your profile page , click \"?\" button for more info");
        panel2.add(userKeyTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testIdTextField = new JTextField();
        testIdTextField.setEditable(false);
        testIdTextField.setEnabled(true);
        testIdTextField.setToolTipText("Test id of current test");
        panel3.add(testIdTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), new Dimension(70, -1), new Dimension(70, -1), 0, false));
        testNameTextField = new JTextField();
        testNameTextField.setAutoscrolls(false);
        testNameTextField.setEditable(false);
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
        cloudPanel = new JPanel();
        cloudPanel.setLayout(new GridLayoutManager(5, 5, new Insets(1, 1, 1, 1), -1, -1));
        cloudPanel.setEnabled(true);
        cloudPanel.setVisible(true);
        mainPanel.add(cloudPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cloudPanel.setBorder(BorderFactory.createTitledBorder("Run in the Cloud Settings"));
        final JLabel label5 = new JLabel();
        label5.setRequestFocusEnabled(false);
        label5.setText("Users #");
        cloudPanel.add(label5, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Location");
        cloudPanel.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel6, new GridConstraints(2, 1, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        numberOfUserTextBox = new JTextField();
        numberOfUserTextBox.setEditable(true);
        numberOfUserTextBox.setEnabled(true);
        numberOfUserTextBox.setText("0");
        numberOfUserTextBox.setToolTipText("Number of users for testing in cloud");
        panel6.add(numberOfUserTextBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(40, -1), new Dimension(40, -1), new Dimension(40, -1), 0, false));
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
        numberOfUsersSlider.setToolTipText("Set number of test users");
        numberOfUsersSlider.setValue(1);
        numberOfUsersSlider.setValueIsAdjusting(true);
        numberOfUsersSlider.putClientProperty("JSlider.isFilled", Boolean.FALSE);
        numberOfUsersSlider.putClientProperty("html.disable", Boolean.FALSE);
        numberOfUsersSlider.putClientProperty("Slider.paintThumbArrowShape", Boolean.FALSE);
        panel6.add(numberOfUsersSlider, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel7, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        locationComboBox = new JComboBox();
        locationComboBox.setDoubleBuffered(true);
        locationComboBox.setEditable(false);
        locationComboBox.setEnabled(true);
        locationComboBox.setToolTipText("Select location");
        panel7.add(locationComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enginesDescription = new JTextField();
        enginesDescription.setEditable(false);
        enginesDescription.setEnabled(false);
        enginesDescription.setText("JMETER CONSOLE");
        enginesDescription.setToolTipText("Number of JMeter engines");
        cloudPanel.add(enginesDescription, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        overridesPanel = new JPanel();
        overridesPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(overridesPanel, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rampupSpinner = new JSpinner();
        rampupSpinner.setAutoscrolls(false);
        rampupSpinner.setFocusTraversalPolicyProvider(true);
        rampupSpinner.setToolTipText("How quickly will load increase");
        panel8.add(rampupSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), new Dimension(50, -1), new Dimension(50, -1), 0, false));
        final JLabel label7 = new JLabel();
        label7.setRequestFocusEnabled(false);
        label7.setText("Rampup Period (seconds)");
        label7.setToolTipText("How quickly will load increase");
        panel8.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel9, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        iterationsSpinner = new JSpinner();
        iterationsSpinner.setToolTipText("\"0\" means \"FOREVER\"");
        panel9.add(iterationsSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), new Dimension(50, -1), new Dimension(50, -1), 0, false));
        final JLabel label8 = new JLabel();
        label8.setRequestFocusEnabled(false);
        label8.setText("# Iterations");
        label8.setToolTipText("\"0\" means \"FOREVER\"");
        panel9.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel10, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        durationSpinner = new JSpinner();
        durationSpinner.setToolTipText("\"0\" means \"Limited by Test Session Time\"");
        panel10.add(durationSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), new Dimension(50, -1), new Dimension(50, -1), 0, false));
        final JLabel label9 = new JLabel();
        label9.setRequestFocusEnabled(false);
        label9.setText("Duration (minutes)");
        label9.setToolTipText("\"0\" means \"Limited by Test Session Time\"");
        panel10.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        runInTheCloud = new JButton();
        runInTheCloud.setActionCommand("start");
        runInTheCloud.setEnabled(false);
        runInTheCloud.setFont(new Font(runInTheCloud.getFont().getName(), runInTheCloud.getFont().getStyle(), 16));
        runInTheCloud.setHideActionText(false);
        runInTheCloud.setInheritsPopupMenu(true);
        runInTheCloud.setLabel("Run in the Cloud!");
        runInTheCloud.setText("Run in the Cloud!");
        runInTheCloud.setToolTipText("Update settings on server and start test");
        cloudPanel.add(runInTheCloud, new GridConstraints(0, 3, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(275, 40), new Dimension(275, 40), new Dimension(275, 40), 0, false));
        editJMXLocallyButton = new JButton();
        editJMXLocallyButton.setText("Edit JMX ");
        editJMXLocallyButton.setToolTipText("Download JMX from server and open");
        cloudPanel.add(editJMXLocallyButton, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        saveUploadButton = new JButton();
        saveUploadButton.setText("Save/Upload JMX");
        saveUploadButton.setToolTipText("Upload JMX to server");
        cloudPanel.add(saveUploadButton, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(165, 25), new Dimension(165, 25), new Dimension(165, 25), 0, false));
        addFilesButton = new JButton();
        addFilesButton.setActionCommand("Add Files for Cloud Test");
        addFilesButton.setEnabled(false);
        addFilesButton.setLabel("Add Files for Cloud Test");
        addFilesButton.setText("Add Files for Cloud Test");
        addFilesButton.setToolTipText("Add data files for test");
        cloudPanel.add(addFilesButton, new GridConstraints(4, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(275, 25), new Dimension(275, 25), new Dimension(275, 25), 0, false));
        mainPanel.add(jMeterPropertyPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jMeterPropertyPanel.setBorder(BorderFactory.createTitledBorder("JMeter Properties"));
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
