package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.common.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/29/12
 * Time: 12:29
 */
public class TestPanelGui {
    public static final String NEW_TEST_ID = "---NEW---";
    public static final String HELP_URL = "http://community.blazemeter.com/knowledgebase/articles/83191-blazemeter-plugin-to-jmeter#user_key";
    //    public static final String REGISTER_URL = "http://www.blazemeter.com/?utm_source=JMeter%2BApplication&utm_medium=cpc&utm_term=BlazeMeter%2BUploader%2BV1&utm_content=V1&utm_campaign=BMJMeterUploader";
    private JTextField userKeyTextField;
    private JTextField reportNameTextField;
    private JTextField testNameTextField;
    private JComboBox testIdComboBox;
    public JPanel mainPanel;
    private JTextField testIdTextField;
    private JButton reloadButton;
    private JButton signUpToBlazemeterButton;
    //    private JLabel infoLabel;
    private JButton createNewButton;
    private JButton goToTestPageButton;
    private JButton helpButton;
    private JSlider numberOfUsersSlider;
    private JTextField numberOfUserTextBox;
    private JButton runInTheCloudButton;
    private JTextField enginesDescription;
    private JComboBox locationComboBox;
    private JPanel cloudPanel;
    private JButton saveSettingsInCloud;
    private JButton runInTheCloud;

    private void fetchUserTestsAsync(String id) {
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
                }
                setTestInfo(BmTestManager.getInstance().getTestInfo());
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

    public TestPanelGui() {
        if (BmTestManager.getInstance().isUserKeyFromProp()) {
            String key = BmTestManager.getInstance().getUserKey();
            if (key.length() >= 20)
                key = key.substring(0, 5) + "**********" + key.substring(14, key.length());
            setUserKey(key);
            userKeyTextField.setEnabled(false);
            userKeyTextField.setToolTipText("User key found in jmeter.properties file");
            signUpToBlazemeterButton.setVisible(false);
            fetchUserTestsAsync(null);
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
                            fetchUserTestsAsync(null);
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
                        TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(BmTestManager.getInstance().getUserKey(), selected.toString(), true);
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
                String id = null;
                if (testIdComboBox.getSelectedItem().getClass().equals(TestInfo.class))
                    id = ((TestInfo) (testIdComboBox.getSelectedItem())).id;
                fetchUserTestsAsync(id);
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
            }
        });

        BmTestManager.getInstance().testUserKeyNotificationListeners.add(new BmTestManager.TestUserKeyNotification() {
            @Override
            public void onTestUserKeyChanged(String userKey) {
                setUserKey(userKey);
                signUpToBlazemeterButton.setEnabled(userKey == null || userKey.isEmpty());
            }
        });

        signUpToBlazemeterButton.setEnabled(BmTestManager.getInstance().getUserKey() == null || BmTestManager.getInstance().getUserKey().isEmpty());

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


        BmTestManager.getInstance().statusChangedNotifications.add(new BmTestManager.StatusChangedNotification() {
            @Override
            public void onTestStatusChanged() {
                TestInfo ti = BmTestManager.getInstance().getTestInfo();
                switch (ti.status) {
                    case Running:
                    case NotRunning:
                        configureFields(ti);
                        break;
                    case NotFound:
                        JOptionPane.showMessageDialog(mainPanel, ti.error, "Test not found error", JOptionPane.ERROR_MESSAGE);
                        break;
                    case Error:
                        JOptionPane.showMessageDialog(mainPanel, ti.error, "Error", JOptionPane.ERROR_MESSAGE);
                        break;
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
        numberOfUsersSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int numberOfUsers = numberOfUsersSlider.getValue();
                int engines = 1;
                numberOfUserTextBox.setText(Integer.toString(numberOfUsers));
                if (numberOfUsers <= 300) {
                    enginesDescription.setText(engines + " MEDIUM engine");
                } else if (numberOfUsers <= 2400) {
                    engines = numberOfUsers / 300;
                    if (numberOfUsers % 300 > 0) {
                        engines++;
                    }
                    enginesDescription.setText(engines + " MEDIUM engines");
                } else {

                    engines = numberOfUsers / 600;
                    if (numberOfUsers % 600 > 0) {
                        engines++;
                    }
                    enginesDescription.setText(engines + " LARGE engines");

                }
            }
        });

        saveSettingsInCloud.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BlazemeterApi.getInstance().updateTestSettings(BmTestManager.getInstance().getUserKey(),
                        BmTestManager.getInstance().getTestInfo().id,
                        locationComboBox.getSelectedItem().toString(),
                        numberOfUsersSlider.getValue());

            }
        });
        runInTheCloud.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = BmTestManager.getInstance().runInTheCloud();
                if (id != -1) {
                    String url = BmTestManager.getInstance().getTestUrl();
                    if (url != null)
                        Utils.Navigate(url);
                }

            }
        });
    }

    private void setTestInfo(TestInfo testInfo) {
        if (testInfo == null || testInfo.isEmpty() || !testInfo.isValid()) {
            testIdComboBox.setSelectedItem(NEW_TEST_ID);
            cloudPanel.setVisible(false);
            configureFields(null);
        } else {
            testIdComboBox.setSelectedItem(testInfo);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateCloudPanel();
                }
            }).start();
            configureFields(testInfo);
        }
    }

    private void updateCloudPanel() {
        cloudPanel.setVisible(false);
        TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(BmTestManager.getInstance().getUserKey(), BmTestManager.getInstance().getTestInfo().id, true);
        this.numberOfUsersSlider.setValue(ti.getNumberOfUsers());
        this.locationComboBox.setSelectedItem(ti.getLocation());
        cloudPanel.setVisible(true);
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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setAutoscrolls(true);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 17, new Insets(1, 1, 1, 1), -1, -1));
        panel1.setVisible(true);
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.windowBorder), "Info"));
        final JLabel label1 = new JLabel();
        label1.setText("User Key");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Test Info");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Report Name");
        panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        reportNameTextField = new JTextField();
        reportNameTextField.setText("sample.jtl");
        reportNameTextField.setToolTipText("Report name that listener will create and use for uploading statistics to it.");
        panel1.add(reportNameTextField, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        signUpToBlazemeterButton = new JButton();
        signUpToBlazemeterButton.setActionCommand("Sign up to BlazeMeter!");
        signUpToBlazemeterButton.setLabel("Sign up to BlazeMeter's free-tier. It's immediate and no credit card is required.");
        signUpToBlazemeterButton.setText("Sign up to BlazeMeter's free-tier. It's immediate and no credit card is required.");
        signUpToBlazemeterButton.setToolTipText("Register/Login to BlazeMeter site and find your User Key on a profile page.");
        panel1.add(signUpToBlazemeterButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        goToTestPageButton = new JButton();
        goToTestPageButton.setActionCommand("Go to Test Page!");
        goToTestPageButton.setEnabled(true);
        goToTestPageButton.setHideActionText(false);
        goToTestPageButton.setText("Go to Test Page!");
        goToTestPageButton.setToolTipText("Navigate to test page on Blazemeter site");
        goToTestPageButton.setVisible(true);
        panel1.add(goToTestPageButton, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Select Test");
        panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        userKeyTextField = new JTextField();
        userKeyTextField.setText("");
        userKeyTextField.setToolTipText("User key - can be found on your profile page , click \"?\" button for more info");
        panel2.add(userKeyTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        panel2.add(helpButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testIdTextField = new JTextField();
        testIdTextField.setEditable(true);
        testIdTextField.setEnabled(false);
        testIdTextField.setToolTipText("Test id of current test");
        panel3.add(testIdTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), new Dimension(70, -1), new Dimension(70, -1), 0, false));
        testNameTextField = new JTextField();
        testNameTextField.setAutoscrolls(false);
        testNameTextField.setToolTipText("Test name of current test");
        panel3.add(testNameTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        panel3.add(createNewButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testIdComboBox = new JComboBox();
        testIdComboBox.setDoubleBuffered(true);
        testIdComboBox.setEditable(true);
        testIdComboBox.setEnabled(true);
        testIdComboBox.setToolTipText("Enter test id  or select test from list , click refresh button if you want to load test list from the server.");
        panel4.add(testIdComboBox, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        panel4.add(reloadButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(24, 21), null, null, 0, false));
        cloudPanel = new JPanel();
        cloudPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 28, new Insets(1, 1, 1, 1), -1, -1));
        cloudPanel.setEnabled(true);
        cloudPanel.setVisible(true);
        mainPanel.add(cloudPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cloudPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.windowBorder), "Run In The Cloud"));
        final JLabel label5 = new JLabel();
        label5.setRequestFocusEnabled(false);
        label5.setText("Users #");
        cloudPanel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 28), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Location");
        cloudPanel.add(label6, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        numberOfUserTextBox = new JTextField();
        numberOfUserTextBox.setEditable(true);
        numberOfUserTextBox.setEnabled(false);
        numberOfUserTextBox.setText("300");
        numberOfUserTextBox.setToolTipText("Test id of current test");
        panel5.add(numberOfUserTextBox, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), new Dimension(70, -1), new Dimension(70, -1), 0, false));
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
        panel5.add(numberOfUsersSlider, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cloudPanel.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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
        panel6.add(locationComboBox, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        cloudPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(4, 10, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        runInTheCloud = new JButton();
        runInTheCloud.setText("Run In The Cloud!");
        cloudPanel.add(runInTheCloud, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveSettingsInCloud = new JButton();
        saveSettingsInCloud.setActionCommand("Run In The Cloud!");
        saveSettingsInCloud.setEnabled(true);
        saveSettingsInCloud.setHideActionText(false);
        saveSettingsInCloud.setText("Save");
        saveSettingsInCloud.setToolTipText("Navigate to test page on Blazemeter site");
        saveSettingsInCloud.setVisible(true);
        cloudPanel.add(saveSettingsInCloud, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(150, -1), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        cloudPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        enginesDescription = new JTextField();
        enginesDescription.setEditable(false);
        enginesDescription.setEnabled(false);
        enginesDescription.setText("1 MEDIUM engine");
        cloudPanel.add(enginesDescription, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        label1.setLabelFor(userKeyTextField);
        label2.setLabelFor(testNameTextField);
        label3.setLabelFor(reportNameTextField);
        label4.setLabelFor(testIdComboBox);
        label5.setLabelFor(testNameTextField);
        label6.setLabelFor(testIdComboBox);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
