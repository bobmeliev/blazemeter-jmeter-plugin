package com.blazemeter.jmeter.testexecutor.listeners;

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

    public UserKeyListener(JTextField userKeyTextField) {
        this.userKeyTextField = userKeyTextField;
    }

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        GuiUtils.validUserKeyField(userKeyTextField);
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
        GuiUtils.validUserKeyField(userKeyTextField);
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        GuiUtils.validUserKeyField(userKeyTextField);
    }


}
