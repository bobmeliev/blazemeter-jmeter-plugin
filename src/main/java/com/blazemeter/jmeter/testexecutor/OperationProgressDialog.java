package com.blazemeter.jmeter.testexecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 7/3/13
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperationProgressDialog extends JDialog {
    public OperationProgressDialog() {
        super();
        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setIndeterminate(true);
        final JOptionPane optionPane = new JOptionPane(jProgressBar, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        this.setTitle("Please, wait...");
        this.setModal(true);
        Container contentPane = this.getContentPane();
        JLabel jLabel = new JLabel("Bla-bla-bla");
        contentPane.add(jLabel, BorderLayout.NORTH);
        contentPane.add(optionPane, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    public void init() {
        //create timer to dispose of dialog after 5 seconds
        Timer timer = new Timer(20000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                OperationProgressDialog.this.dispose();
            }
        });
        timer.setRepeats(false);//the timer should only go off once
//start timer to close JDialog as dialog modal we must start the timer before its visible
        timer.start();

        this.setSize(200, 100);
        this.setLocationRelativeTo(null);
        this.setVisible(true);


    }
}
