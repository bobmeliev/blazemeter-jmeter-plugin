package com.blazemeter.jmeter.results;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.Constants;
import com.blazemeter.jmeter.testinfo.TestInfo;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LogUploader {
    private static LogUploader instance;
    private final HashMap<String, String> log_files = new HashMap<String, String>();
    private final HashMap<String, BufferedReader> logFileReaders = new HashMap<String, BufferedReader>();
    private boolean uploadFinished;
    private boolean isRunning = false;

    private LogUploader() {
    }

    public static LogUploader getInstance() {
        if (instance == null)
            instance = new LogUploader();
        return instance;
    }

    public void startListening() {
        if (isRunning)
            return;

        log_files.put("jmeter_log", JMeterUtils.getPropDefault(LoggingManager.LOG_FILE, "jmeter.log"));
        if (Utils.isJMeterServer()) {
            log_files.put("jmeter_server_log", Constants.JMETER_SERVER_LOG_FILENAME);
        }
        for (Map.Entry<String, String> entry : log_files.entrySet()) {
            StringBuilder filename = new StringBuilder(entry.getValue());
            if (!new File(filename.toString()).exists()) {
                filename = new StringBuilder(JMeterUtils.getJMeterBinDir() + "/").append(filename);
            }
            try {
                String host = Utils.getHostIP() + (Utils.isJMeterServer() ? "(jmeter-server)" : "");
                BmLog.console("Log file path at host= " + host + " is: " + filename);

                BufferedReader logBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename.toString())));
                logFileReaders.put(entry.getKey(), logBufferedReader);
                uploadLog(filename.toString(), logBufferedReader);
            } catch (FileNotFoundException fnfe) {
                BmLog.error("Failed to find log file " + filename + ": " + fnfe.getMessage());
            }
        }
        isRunning = true;
    }


    private void uploadLog(String logFilename, BufferedReader logReader) {
        uploadFinished = false;
        boolean last = true;
        while (isRunning || last) {
            StringBuilder buff = new StringBuilder(4096);
            String line;
            try {
                while ((line = logReader.readLine()) != null) {
                    buff.append(line);
                    buff.append("\n");
                }
            } catch (IOException e) {
                BmLog.error("Problems with reading " + logFilename, e);
            }
            if (buff.length() > 0) {
                logFilename = logFilename.substring(logFilename.lastIndexOf(System.getProperty("file.separator")) + 1);
                upload(Utils.getHostIP() + "_" + logFilename, buff.toString(), "log");
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
            last = !isRunning && !last;
        }
        uploadFinished = true;

    }

    public void stopListening() {
        if (!isRunning)
            return;

        isRunning = false;
        while (!this.uploadFinished) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        for (Map.Entry<String, BufferedReader> entry : logFileReaders.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                BmLog.error("Failed to close " + entry.getKey() + " reader");
            }
        }
    }

    private void upload(String logName, String data, String dataType) {
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        String testId = testInfo.getId();
        BmLog.debug(String.format("Log uploader sending log:%s , %d bytes  ", logName, data.length()));
        BmTestManager.getInstance().logUpload(testId, logName, data, dataType);
    }
}
