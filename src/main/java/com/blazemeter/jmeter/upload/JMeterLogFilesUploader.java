package com.blazemeter.jmeter.upload;

import com.blazemeter.jmeter.testexecutor.Constants;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;
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
            String server_log_file_path = JMeterUtils.getProperty(Constants.BLAZEMETER_SERVER_LOG_FILE);
            if (server_log_file_path == null || server_log_file_path.isEmpty())   //No log file!
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
        String jmeter_log_filename = getJMeterLogFilename();
        if (jmeter_log_filename != null) {
            try {
                jmeter_log_reader = new BufferedReader(new InputStreamReader(new FileInputStream(jmeter_log_filename)));
            } catch (FileNotFoundException fnfe) {
                BmLog.error("Could not upload log file" + jmeter_log_filename + ", file not found!", fnfe);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UploadJMeterLog();
                }
            }).start();
        } else {
            BmLog.error("Jmeter log file was not defined in jmeter.properties:jmeter server log will not be uploaded");
        }

        /* if ThreadGroup has name "RMI Runtime", than this jmeter instance is jmeter-server.
          Only in this case we need to upload jmeter-server.log
        */
        if (Thread.currentThread().getThreadGroup().getName().equals("RMI Runtime")) {
            String jmeter_server_log_filename = getJMeterServerLogFilename();
            if (jmeter_server_log_filename != null) {
                try {
                    jmeter_server_log_reader = new BufferedReader(new InputStreamReader(new FileInputStream(jmeter_server_log_filename)));
                } catch (FileNotFoundException fnfe) {
                    BmLog.error("Could not upload log file" + jmeter_server_log_filename + ", file not found!", fnfe);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UploadJmeterServerLog();
                    }
                }).start();
            } else {
                BmLog.error("Jmeter server log file was not defined in jmeter.properties");
            }
        }
        isRunning = true;
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
                BmLog.error("Problems with reading " + jmeter_log_filename, e);
            }
            if (buff.length() > 0) {
                jmeter_log_filename = jmeter_log_filename.substring(jmeter_log_filename.lastIndexOf(System.getProperty("file.separator")) + 1);

                if (Thread.currentThread().getThreadGroup().getName().equals("main")) {
                    new LogUploader(Utils.getHostIP() + "_" + jmeter_log_filename, buff.toString(), "log").run();
                }
                if (Thread.currentThread().getThreadGroup().getName().equals("RMI Runtime")) {
                    new LogUploader(Utils.getHostIP() + "_" + jmeter_log_filename, buff.toString(), "log").run();
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
        if (Thread.currentThread().getThreadGroup().getName().equals("RMI Runtime")) {

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
                    new LogUploader(Utils.getHostIP() + "_" + jmeter_server_log_filename, jmeter_log_buff.toString(), "log").run();
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
                last = !isRunning && !last;

            }
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
