package com.blazemeter.jmeter.utils;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;

import java.io.*;

public class JMeterLogFilesUploader {
    private static JMeterLogFilesUploader instance;
    private BufferedReader jmeter_log_reader;
    private BufferedReader jmeter_server_log_reader;
    String jmeter_server_log_filename = null;
    private String jmeter_log_filename = null;
    private boolean uploadFinished;
    private boolean isRunning = false;

    private JMeterLogFilesUploader() {
    }


    public static JMeterLogFilesUploader getInstance() {
        if (instance == null)
            instance = new JMeterLogFilesUploader();
        return instance;
    }


    public String getJMeterLogFilename() {
        if (jmeter_log_filename == null) {
            String log_file_path = JMeterUtils.getPropDefault(LoggingManager.LOG_FILE, "jmeter.log");
            if (log_file_path.equals(""))   //No log file!
                return null;

            if (new File(log_file_path).exists()) {
                jmeter_log_filename = log_file_path;
            } else {
                log_file_path = JMeterUtils.getJMeterBinDir() + "/" + log_file_path;
                if (new File(log_file_path).exists())
                    jmeter_log_filename = log_file_path;
            }
            BmLog.console("Log file path is: " + jmeter_log_filename);
        }
        return jmeter_log_filename;
    }

    public String getJMeterServerLogFilename() {
        if (jmeter_server_log_filename == null) {
            String server_log_file_path = JMeterUtils.getProperty("server_log_file");
            if (server_log_file_path.equals(""))   //No log file!
                return null;

            if (new File(server_log_file_path).exists()) {
                jmeter_server_log_filename = server_log_file_path;
            } else {
                server_log_file_path = JMeterUtils.getJMeterBinDir() + "/" + server_log_file_path;
                if (new File(server_log_file_path).exists())
                    jmeter_server_log_filename = server_log_file_path;
            }
            BmLog.console("Sever log file path is: " + jmeter_server_log_filename);
        }
        return jmeter_server_log_filename;
    }


    public void startListening() {
        if (isRunning)
            return;
        String jmeter_server_log_filename = "";
        if (Thread.currentThread().getThreadGroup().getName().equals("RMI Runtime")) {
            jmeter_server_log_filename = getJMeterServerLogFilename();
        }
        String jmeter_log_filename = getJMeterLogFilename();

        if (jmeter_log_filename == null)
            return;

        try {
            jmeter_log_reader = new BufferedReader(new InputStreamReader(new FileInputStream(jmeter_log_filename)));
            jmeter_server_log_reader = new BufferedReader(new InputStreamReader(new FileInputStream(jmeter_server_log_filename)));
        } catch (FileNotFoundException fnfe) {
            BmLog.console("Could not upload log file, file not found!");
            BmLog.error("Could not upload log file, file not found!", fnfe);
        }

        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadJMeterLog();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadJmeterServerLog();
            }
        }).start();
    }


    private void UploadJMeterLog() {
        uploadFinished = false;
        boolean last = true;
        while (isRunning || last) {
            StringBuilder buff = new StringBuilder(4096);
            String line;
            try {
                while ((line = jmeter_log_reader.readLine()) != null) {
                    buff.append(line);
                    buff.append("\n");
                }
            } catch (IOException e) {
                BmLog.error(e);
            }
            if (buff.length() > 0) {
                if (Thread.currentThread().getThreadGroup().getName().equals("main")) {
                    Uploader.getInstance().ForceUpload("console_" + jmeter_log_filename, buff.toString(), "log");
                }
                if (Thread.currentThread().getThreadGroup().getName().equals("RMI Runtime")) {
                    Uploader.getInstance().ForceUpload(Utils.getHostIP() + "_" + jmeter_log_filename, buff.toString(), "log");
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
            last = !isRunning && !last;

        }
        uploadFinished = true;

    }


    private void UploadJmeterServerLog() {
        uploadFinished = false;
        boolean last = true;
        while (isRunning || last) {
            StringBuilder jmeter_log_buff = new StringBuilder(4096);
            String jmeter_log_line;
            try {
                while ((jmeter_log_line = jmeter_server_log_reader.readLine()) != null) {
                    jmeter_log_buff.append(jmeter_log_line);
                    jmeter_log_buff.append("\n");
                }
            } catch (IOException ioe) {
                BmLog.error("Empty jmeter-server log file: " + ioe);
            } catch (NullPointerException npe) {
                BmLog.error("JMeter server log file was not read: ", npe);
            }
            if (jmeter_log_buff.length() > 0) {
                Uploader.getInstance().ForceUpload(Utils.getHostIP() + "_" + jmeter_server_log_filename, jmeter_log_buff.toString(), "log");

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
            jmeter_log_reader.close();
        } catch (IOException ignored) {
        }
    }
}
