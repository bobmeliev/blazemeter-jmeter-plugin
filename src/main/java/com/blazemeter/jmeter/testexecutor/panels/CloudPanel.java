package com.blazemeter.jmeter.testexecutor.panels;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.ServerStatusController;
import com.blazemeter.jmeter.testexecutor.TestPanelGui;
import com.blazemeter.jmeter.testexecutor.listeners.EditJMXLocallyButtonListener;
import com.blazemeter.jmeter.testexecutor.listeners.SaveUploadButtonListener;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;
import com.blazemeter.jmeter.testexecutor.notifications.IUserInfoChangedNotification;
import com.blazemeter.jmeter.testinfo.*;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Constants;
import com.blazemeter.jmeter.utils.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.util.JMeterUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/23/13
 * Time: 8:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class CloudPanel extends JPanel {
    private JTextField numberOfUserTextBox;
    private JSlider numberOfUsersSlider;
    private JComboBox locationComboBox;
    private JTextArea enginesDescription;
    private JPanel overridesPanel;
    private JSpinner rampupSpinner;
    private JSpinner iterationsSpinner;
    private JSpinner durationSpinner;
    private JButton runInTheCloud;
    private JButton editJMXLocallyButton;
    private JButton saveUploadButton;
    private JButton addFilesButton;

    public CloudPanel() {
        createGui();
        init();
    }


    private void init() {
        locationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String locationId = Utils.getLocationId((String) locationComboBox.getSelectedItem());
                if (!locationId.isEmpty()) {
                    BmTestManager.getInstance().getTestInfo().setLocation(locationId);
                }
            }
        });


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


        numberOfUsersSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int numberOfUsers = numberOfUsersSlider.getValue();
                int engines;
                String engineSize;
                int usersPerEngine;
                TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
                testInfo.setNumberOfUsers(numberOfUsers);
                HashMap<String, String> enginesParameters = Utils.countEngines(numberOfUsers);
                engines = Integer.valueOf(enginesParameters.get(Constants.ENGINES));
                engineSize = enginesParameters.get(Constants.ENGINE_SIZE).equals("m1.medium") ? "MEDIUM ENGINE" : "LARGE ENGINE";
                usersPerEngine = Integer.valueOf(enginesParameters.get(Constants.USERS_PER_ENGINE));
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
                    dialogButton = JOptionPane.showConfirmDialog(TestPanelGui.getGui().getMainPanel(), "Are you sure that you want to start the test?",
                            "Start test?",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogButton == JOptionPane.YES_OPTION) {
                        startInTheCloud();
                    }
/*

                   OperationProgressDialog operationProgressDialog = new OperationProgressDialog("Please, wait...",
                                "Operation will take a few seconds to execute. Your patience is appreciated.");
                        operationProgressDialog.windowOpened(new WindowEvent(operationProgressDialog,WindowEvent.WINDOW_OPENED));
                        operationProgressDialog.windowClosing(new WindowEvent(operationProgressDialog,WindowEvent.WINDOW_CLOSING));

*/


                } else {
                    dialogButton = JOptionPane.showConfirmDialog(TestPanelGui.getGui().getMainPanel(), "Are you sure that you want to stop the test? ",
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
        editJMXLocallyButton.addActionListener(new EditJMXLocallyButtonListener());


        //Here should be all changes of TestInfo processed
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.testInfoNotificationListeners.add(new ITestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                if (testInfo == null) {
                    return;
                }

                if (testInfo.getStatus() == TestStatus.Running) {
                    Utils.enableElements(CloudPanel.this, false);
                    runInTheCloud.setEnabled(true);
                }

                if ((testInfo.getStatus() == TestStatus.NotRunning)) {
                    boolean isTestIdEmpty = testInfo.getId().isEmpty();
                    Utils.enableElements(CloudPanel.this, !isTestIdEmpty);
                    runInTheCloud.setEnabled(!isTestIdEmpty);


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
                        boolean testIsRunning = testInfo.getStatus() == TestStatus.Running;
                        runInTheCloud.setEnabled(!testIsRunning);
                        break;
                    case NOT_AVAILABLE:
                        runInTheCloud.setEnabled(false);
                        break;
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
                HashMap<String, String> enginesParams = Utils.countEngines(numberOfUsers);
                engines = Integer.valueOf(enginesParams.get(Constants.ENGINES));
                engineSize = enginesParams.get(Constants.ENGINE_SIZE).equals("m1.medium") ? "MEDIUM ENGINE" : "LARGE ENGINE";
                usersPerEngine = Integer.valueOf(enginesParams.get(Constants.USERS_PER_ENGINE));
                if (numberOfUsers <= 300) {
                    enginesDescription.setText(String.format("JMETER CONSOLE -  %d users", usersPerEngine));
                    numberOfUserTextBox.setText(Integer.toString(numberOfUsers));
                } else {
                    enginesDescription.setText(String.format("%d %s x %d users", engines, engineSize, usersPerEngine));
                    numberOfUserTextBox.setText(Integer.toString(usersPerEngine * engines));
                }
            }
        });
        BmTestManager.getInstance().userInfoChangedNotificationListeners.add(new IUserInfoChangedNotification() {
            @Override
            public void onUserInfoChanged(UserInfo userInfo) {
                if (userInfo == null) {
                    return;
                } else { /*
                    if (userInfo.getMaxUsersLimit() > 8400 && userInfo.getMaxEnginesLimit() > 14) {
                        userInfo.setMaxUsersLimit(8400);
                        userInfo.setMaxEnginesLimit(14);
                    }*/
                    //configure numberOfUserSlider depending on UserInfo
                    numberOfUsersSlider.setMinimum(0);
                    numberOfUsersSlider.setMaximum(userInfo.getMaxUsersLimit());
                    numberOfUsersSlider.setMajorTickSpacing(userInfo.getMaxUsersLimit() / 4);
                    numberOfUsersSlider.setMinorTickSpacing(userInfo.getMaxUsersLimit() / 12);
                    Dictionary labels = numberOfUsersSlider.createStandardLabels(numberOfUsersSlider.getMajorTickSpacing());
                    numberOfUsersSlider.setLabelTable(labels);

                    //set locations list
                    JSONArray locations = userInfo.getLocations();
                    setLocations(locations);
                }
            }
        });

    }


    private void createGui() {
        this.setLayout(new GridLayoutManager(5, 5, new Insets(1, 1, 1, 1), -1, -1));
        this.setEnabled(true);
        this.setVisible(true);
        this.setBorder(BorderFactory.createTitledBorder("Run in the Cloud Settings"));
        final JLabel label1 = new JLabel();
        label1.setRequestFocusEnabled(false);
        label1.setText("Users #");
        this.add(label1, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Location");
        this.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.add(panel2, new GridConstraints(2, 1, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        numberOfUserTextBox = new JTextField();
        numberOfUserTextBox.setEditable(true);
        numberOfUserTextBox.setEnabled(true);
        numberOfUserTextBox.setText("0");
        numberOfUserTextBox.setToolTipText("Number of users for testing in cloud");
        panel2.add(numberOfUserTextBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(40, -1), new Dimension(40, -1), new Dimension(40, -1), 0, false));
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
        panel2.add(numberOfUsersSlider, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.add(panel3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        locationComboBox = new JComboBox();
        locationComboBox.setDoubleBuffered(true);
        locationComboBox.setEditable(false);
        locationComboBox.setEnabled(true);
        locationComboBox.setToolTipText("Select location");
        final DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
        defaultComboBoxModel.addElement("EU West (Ireland)");
        defaultComboBoxModel.addElement("US East (Virginia)");
        defaultComboBoxModel.addElement("US West (N.California)");
        defaultComboBoxModel.addElement("US West (Oregon)");
        defaultComboBoxModel.addElement("Asia Pacific (Singapore)");
        defaultComboBoxModel.addElement("Japan (Tokyo)");
        defaultComboBoxModel.addElement("South America (San Paulo)");
        defaultComboBoxModel.addElement("Australia (Sydney)");
        locationComboBox.setModel(defaultComboBoxModel);
        panel3.add(locationComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enginesDescription = new JTextArea(Constants.EMPTY);
        Border border = BorderFactory.createLineBorder(Color.GRAY, 1);
//        enginesDescription.setBackground(Color.);
        enginesDescription.setBorder(border);
        enginesDescription.setText("JMETER CONSOLE");
        enginesDescription.setToolTipText("Number of JMeter engines");
        this.add(enginesDescription, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        overridesPanel = new JPanel();
        overridesPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.add(overridesPanel, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rampupSpinner = new JSpinner();
        rampupSpinner.setAutoscrolls(false);
        rampupSpinner.setFocusTraversalPolicyProvider(true);
        rampupSpinner.setToolTipText("How quickly will load increase");
        panel4.add(rampupSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), new Dimension(50, -1), new Dimension(50, -1), 0, false));
        final JLabel label3 = new JLabel();
        label3.setRequestFocusEnabled(false);
        label3.setText("Rampup Period (seconds)");
        label3.setToolTipText("How quickly will load increase");
        panel4.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        iterationsSpinner = new JSpinner();
        iterationsSpinner.setToolTipText("\"0\" means \"FOREVER\"");
        panel5.add(iterationsSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), new Dimension(50, -1), new Dimension(50, -1), 0, false));
        final JLabel label4 = new JLabel();
        label4.setRequestFocusEnabled(false);
        label4.setText("# Iterations");
        label4.setToolTipText("\"0\" means \"FOREVER\"");
        panel5.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        overridesPanel.add(panel6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        durationSpinner = new JSpinner();
        durationSpinner.setToolTipText("\"0\" means \"Limited by Test Session Time\"");
        panel6.add(durationSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), new Dimension(50, -1), new Dimension(50, -1), 0, false));
        final JLabel label5 = new JLabel();
        label5.setRequestFocusEnabled(false);
        label5.setText("Duration (minutes)");
        label5.setToolTipText("\"0\" means \"Limited by Test Session Time\"");
        panel6.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        runInTheCloud = new JButton();
        runInTheCloud.setActionCommand("start");
        runInTheCloud.setEnabled(false);
        runInTheCloud.setFont(new Font(runInTheCloud.getFont().getName(), runInTheCloud.getFont().getStyle(), 16));
        runInTheCloud.setHideActionText(false);
        runInTheCloud.setInheritsPopupMenu(true);
        runInTheCloud.setLabel("Run in the Cloud!");
        runInTheCloud.setText("Run in the Cloud!");
        runInTheCloud.setToolTipText("Update settings on server and start test");
        this.add(runInTheCloud, new GridConstraints(0, 3, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(275, 40), new Dimension(275, 40), new Dimension(275, 40), 0, false));
        editJMXLocallyButton = new JButton();
        editJMXLocallyButton.setText("Edit JMX ");
        editJMXLocallyButton.setToolTipText("Download JMX from server and open");
        this.add(editJMXLocallyButton, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        saveUploadButton = new JButton();
        saveUploadButton.setText("Save/Upload JMX");
        saveUploadButton.setToolTipText("Upload JMX to server");
        this.add(saveUploadButton, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(165, 25), new Dimension(165, 25), new Dimension(165, 25), 0, false));
        addFilesButton = new JButton();
        addFilesButton.setActionCommand("Add Files for Cloud Test");
        addFilesButton.setEnabled(false);
        addFilesButton.setLabel("Add Files for Cloud Test");
        addFilesButton.setText("Add Files for Cloud Test");
        addFilesButton.setToolTipText("Add data files for test");
        this.add(addFilesButton, new GridConstraints(4, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(275, 25), new Dimension(275, 25), new Dimension(275, 25), 0, false));

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

    private void saveCloudTest() {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        int numberOfUsers = numberOfUsersSlider.getValue();

        HashMap<String, String> enginesParams = Utils.countEngines(numberOfUsers);
        int userPerEngine = Integer.valueOf(enginesParams.get(Constants.USERS_PER_ENGINE));

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
            JMeterPropertyPanel propertyPanel = (JMeterPropertyPanel) TestPanelGui.getGui().getjMeterPropertyPanel();
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

    public void setTestInfo(TestInfo testInfo) {
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

    public void reset() {
        numberOfUsersSlider.setValue(0);
        numberOfUserTextBox.setText("0");
        rampupSpinner.setValue(0);
        iterationsSpinner.setValue(0);
        durationSpinner.setValue(0);
        runInTheCloud.setEnabled(false);
        addFilesButton.setEnabled(false);
    }

    public void setLocations(JSONArray locations) {
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

    public String getServerLocation() {
        return (String) locationComboBox.getSelectedItem();
    }

    public int getNumberOfUsers() {
        return numberOfUsersSlider.getValue();
    }

    public void setNumberOfUsers(int numberOfUsers) {
        numberOfUsersSlider.setValue(numberOfUsers);
    }

}
