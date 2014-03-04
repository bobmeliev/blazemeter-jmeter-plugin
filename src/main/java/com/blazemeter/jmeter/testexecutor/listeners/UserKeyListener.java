package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.panels.CloudPanel;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import com.blazemeter.jmeter.utils.GuiUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/30/13
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserKeyListener implements DocumentListener {
    private JTextField userKeyTextField;
    private JButton signUpButton;
    private JComboBox testIdComboBox;
    private JPanel mainPanel;
    private CloudPanel cloudPanel;

    public UserKeyListener(JTextField userKeyTextField,
                           JButton signUpButton,
                           JComboBox testIdComboBox,
                           JPanel mainPanel,
                           CloudPanel cloudPanel) {
        this.userKeyTextField = userKeyTextField;
        this.signUpButton = signUpButton;
        this.testIdComboBox = testIdComboBox;
        this.mainPanel = mainPanel;
        this.cloudPanel = cloudPanel;
    }

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        process();
    }


    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(DocumentEvent e) {
        process();
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        process();
    }

    private void process() {
        if (!GuiUtils.validUserKeyField(userKeyTextField)) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GuiUtils.getUserTests(testIdComboBox, mainPanel, cloudPanel, userKeyTextField.getText());
                signUpButton.setVisible(false);
                BmTestManager.getInstance().getUserInfo(true);
                TestPanel.getTestPanel().enableMainPanelControls(true);
            }
        });
    }
}
