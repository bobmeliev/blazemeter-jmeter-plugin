package com.blazemeter.jmeter.testexecutor.dialogs;

import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/3/13
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperationProgressDialog extends JDialog implements WindowListener, PropertyChangeListener {
    String title = null;
    String message = null;
    TestStatus event = null;

    public OperationProgressDialog(String title, String message, TestStatus event) {
        super();
        this.title = title;
        this.message = message;
        this.event = event;
        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setIndeterminate(true);
        final JOptionPane progressPane = new JOptionPane(jProgressBar, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        this.setTitle(this.title);
        this.setModal(true);
        Container contentPane = this.getContentPane();
        contentPane.add(new JLabel(this.message), BorderLayout.NORTH);
        contentPane.add(progressPane, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(this.message.length() * 8, 130);
        BmTestManager.getInstance().testInfoNotificationListeners.add(new ITestInfoNotification() {
            @Override
            public void onTestInfoChanged(TestInfo testInfo) {
                TestStatus testStatus = testInfo.getStatus();
                if (testStatus != null && testStatus.equals(OperationProgressDialog.this.event)) {
                    OperationProgressDialog.this.windowClosed(new WindowEvent(OperationProgressDialog.this, WindowEvent.WINDOW_CLOSED));
                    BmTestManager.getInstance().testInfoNotificationListeners.remove(this);
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue().equals("STARTED")) {
            this.windowOpened(new WindowEvent(this, WindowEvent.WINDOW_OPENED));
        }
    }

    /**
     * Invoked the first time a window is made visible.
     */
    @Override
    public void windowOpened(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_OPENED) {
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }
    }

    /**
     * Invoked when the user attempts to close the window
     * from the window's system menu.
     */
    @Override
    public void windowClosing(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            this.setVisible(false);
            this.dispose();
        }
    }

    /**
     * Invoked when a window has been closed as the result
     * of calling dispose on the window.
     */
    @Override
    public void windowClosed(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            this.setVisible(false);
            this.dispose();
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when a window is changed from a normal to a
     * minimized state. For many platforms, a minimized window
     * is displayed as the icon specified in the window's
     * iconImage property.
     *
     * @see java.awt.Frame#setIconImage
     */
    @Override
    public void windowIconified(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when a window is changed from a minimized
     * to a normal state.
     */
    @Override
    public void windowDeiconified(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when the Window is set to be the active Window. Only a Frame or
     * a Dialog can be the active Window. The native windowing system may
     * denote the active Window or its children with special decorations, such
     * as a highlighted title bar. The active Window is always either the
     * focused Window, or the first Frame or Dialog that is an owner of the
     * focused Window.
     */
    @Override
    public void windowActivated(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when a Window is no longer the active Window. Only a Frame or a
     * Dialog can be the active Window. The native windowing system may denote
     * the active Window or its children with special decorations, such as a
     * highlighted title bar. The active Window is always either the focused
     * Window, or the first Frame or Dialog that is an owner of the focused
     * Window.
     */
    @Override
    public void windowDeactivated(WindowEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}