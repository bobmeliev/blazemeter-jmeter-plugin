package com.blazemeter.jmeter.testexecutor.notificationsImpl;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.TestInfoController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.notifications.ITestInfoNotification;
import com.blazemeter.jmeter.testexecutor.panels.TestPanel;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by dzmitrykashlach on 12/20/13.
 */
public class TestInfoNotificationTP implements ITestInfoNotification {
    private JRadioButton runLocal;
    private JRadioButton runRemote;
    private JPanel jMeterPropertyPanel;
    private TestPanel testPanel;

    public TestInfoNotificationTP(JRadioButton runLocal, JRadioButton runRemote, JPanel jMeterPropertyPanel) {
        this.runLocal = runLocal;
        this.runRemote = runRemote;
        this.jMeterPropertyPanel = jMeterPropertyPanel;
        this.testPanel = TestPanel.getTestPanel();

    }

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
            runLocal.setEnabled(false);
            runRemote.setEnabled(false);
            Utils.enableElements(jMeterPropertyPanel, false);
        }

        if ((testInfo.getStatus() == TestStatus.NotRunning)) {
            Utils.enableElements(jMeterPropertyPanel, true);
            boolean isTestRunning = BmTestManager.isTestRunning();
            runLocal.setEnabled(!isTestRunning);
            runRemote.setEnabled(!isTestRunning);

            testPanel.configureMainPanel(testInfo);

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

        testPanel.setTestInfo(testInfo);

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
}
