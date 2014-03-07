package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.services.FileServer;

import java.io.File;

/**
 * Created by dzmitrykashlach on 3/7/14.
 */
public class JMXUploader implements Runnable {
    @Override
    public void run() {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        FileServer fileServer = FileServer.getFileServer();
        String projectPath = null;
        if (fileServer.getScriptName() != null) {
            projectPath = fileServer.getBaseDir() + "/" + fileServer.getScriptName();
        } else if (!JMeter.isNonGUI()) {
            projectPath = GuiPackage.getInstance().getTestPlanFile();
        }
        try {
            String filename = new File(projectPath).getName();
            BlazemeterApi.getInstance().uploadJmx(bmTestManager.getUserKey(), bmTestManager.getTestInfo().getId(), filename, projectPath);
        } catch (NullPointerException npe) {
            BmLog.error("JMX was not uploaded to server: test-plan is needed to be saved first ");
        } catch (Exception ex) {
            BmLog.error(ex);

        }
    }
}
