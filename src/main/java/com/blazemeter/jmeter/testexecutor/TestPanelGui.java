package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.common.BlazemeterApi;
import com.blazemeter.jmeter.common.BmLog;
import com.blazemeter.jmeter.common.TestStatus;
import com.blazemeter.jmeter.common.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    private void fetchUserTestsAsync() {
        String userKey = BmTestManager.getInstance().getUserKey();
        if (userKey == null || userKey.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Please enter user key", "No user key", JOptionPane.ERROR_MESSAGE);
            return;
        }

        testIdComboBox.removeAllItems();
        testIdComboBox.addItem(NEW_TEST_ID);
//        infoLabel.setText("Loading your tests list");
        BlazemeterApi.getInstance().getTestsAsync(userKey, new BlazemeterApi.TestContainerNotifier() {
            @Override
            public void testReceived(ArrayList<TestInfo> tests) {
                if (tests != null) {
                    for (TestInfo testInfo : tests) {
                        testIdComboBox.addItem(testInfo);
                    }
                }
//                infoLabel.setText("Finished loading your tests list");
            }
        });
    }

    public void setUserKey(String key) {
        userKeyTextField.setText(key);
        BmLog.console("set key" + key);
    }

    public void addTestId(Object test) {
        addTestId(test, false);
    }

    public void addTestId(Object test, boolean selected) {
        testIdComboBox.addItem(test);
        if (selected) {
            testIdComboBox.setSelectedItem(test);
        }
        BmLog.console("add item" + test.toString());
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
//                Long c = System.currentTimeMillis();
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    Object selected = testIdComboBox.getSelectedItem();
//                    BmLog.console("selected" + selected.toString());
                    if (selected instanceof TestInfo) {
                        TestInfo testInfo = (TestInfo) selected;
                        BmTestManager.getInstance().setTestInfo(testInfo);
                        configureFields(testInfo);
//                        BmLog.console("testIdComboBox1 Took " + (System.currentTimeMillis() - c));
                    } else if (Utils.isInteger(selected.toString())) {
//                        infoLabel.setText("Loading test " + selected.toString() + " info.");
                        TestInfo ti = BlazemeterApi.getInstance().getTestRunStatus(BmTestManager.getInstance().getUserKey(), selected.toString());
                        BmLog.console(ti.toString());
                        if (ti.status == TestStatus.Running || ti.status == TestStatus.NotRunning) {
                            addTestId(ti);
                            setTestInfo(ti);
                        } else {
//                            infoLabel.setText("Test " + selected.toString() + " not found!");
                            JOptionPane.showMessageDialog(mainPanel, ti.error, "Test not found error", JOptionPane.ERROR_MESSAGE);
                            configureFields(null);
                        }
//                        BmLog.console("testIdComboBox2 Took " + (System.currentTimeMillis() - c));
                    } else if (selected.toString().equals(NEW_TEST_ID)) {
                        configureFields(null);
//                        BmLog.console("testIdComboBox3 Took " + (System.currentTimeMillis() - c));
                    }


                }
//                BmLog.console("testIdComboBox111 Took " + (System.currentTimeMillis() - c));
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
                Navigate(BlazemeterApi.BmUrlManager.getServerUrl() + "/user");
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
                    addTestId(testInfo);
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
                    Navigate(url);
            }
        });
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Navigate(HELP_URL);
            }
        });
        addTestId(NEW_TEST_ID, true);
    }

    private void setTestInfo(TestInfo testInfo) {
        if (testInfo == null || testInfo.isEmpty() || !testInfo.isValid())
            testIdComboBox.setSelectedItem(NEW_TEST_ID);
        else {
            testIdComboBox.setSelectedItem(testInfo);
        }
    }

    private void Navigate(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            BmLog.error(e);
        } catch (URISyntaxException e) {
            BmLog.error(e);
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
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label1.setLabelFor(userKeyTextField);
        label2.setLabelFor(testNameTextField);
        label3.setLabelFor(reportNameTextField);
        label4.setLabelFor(testIdComboBox);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
