package com.blazemeter.jmeter.utils;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;

import java.io.*;

public class LogFilesUploader {
    private static LogFilesUploader instance;
    private BufferedReader reader;

    private LogFilesUploader() {
    }

    private boolean isRunning = false;

    public static LogFilesUploader getInstance() {
        if (instance == null)
            instance = new LogFilesUploader();
        return instance;
    }

    String filename = null;

    public String getLogFilename() {
        if (filename == null) {
            String path = JMeterUtils.getPropDefault(LoggingManager.LOG_FILE, "jmeter.log");
            if (path.equals(""))   //No log file!
                return null;

            if (new File(path).exists()) {
                filename = path;
            } else {
                path = JMeterUtils.getJMeterBinDir() + "/" + path;
                if (new File(path).exists())
                    filename = path;
            }
            BmLog.console("Log file path is: " + filename);
        }
        return filename;
    }

    public void startListening() {
        if (isRunning)
            return;

        String filename = getLogFilename();
        if (filename == null)
            return;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        } catch (FileNotFoundException e) {
            BmLog.console("Could not upload log file, file not found!");
        }

        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadLog();
            }
        }).start();

    }

    private boolean uploadFinished;

    private void UploadLog() {
        uploadFinished = false;
        boolean last = true;
        while (isRunning || last) {
            StringBuilder buff = new StringBuilder(4096);
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    buff.append(line);
                    buff.append("\n");
                }
            } catch (IOException e) {
                BmLog.error(e);
            }
            if (buff.length() > 0) {
                Uploader.getInstance().ForceUpload(filename, buff.toString(), "log");
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
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }


}
