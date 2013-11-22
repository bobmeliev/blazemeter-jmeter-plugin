package com.blazemeter.jmeter.testexecutor.listeners;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.Constants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/30/13
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserKeyListener implements DocumentListener {

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        if (isUserKeyValidOnGUI(e)) {
            // fetch tests from server

        }

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
        if (isUserKeyValidOnGUI(e)) {
            // fetch tests from server

        }
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        if (isUserKeyValidOnGUI(e)) {
            // fetch tests from server

        }
    }

    boolean isUserKeyValidOnGUI(DocumentEvent e) {
        JTextField userKeyTextField = (JTextField) e.getDocument().getProperty(Constants.PARENT);
        String userKey = userKeyTextField.getText();
        if (userKey.matches(Constants.USERKEY_REGEX)) {
            Border greyBorder = BorderFactory.createLineBorder(Color.GRAY);
            userKeyTextField.setBorder(greyBorder);
            return true;

        } else {
            Border redBorder = BorderFactory.createLineBorder(Color.RED);
            userKeyTextField.setBorder(redBorder);
            return false;
        }
    }


    private void isUserKeyValidOnServer(String userKey) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.setUserKey(userKey);
//        fetchUserTestsAsync();

    }
}
