package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.SamplingStatus;
import com.blazemeter.jmeter.testinfo.TestInfo;
import org.apache.jmeter.util.JMeterUtils;

import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Uploader {
    private static Uploader instance = new Uploader();
    private int chunk_size;
    private static final int MaxUploadInterval = 15000;

    public static Uploader getInstance() {
        return instance;
    }

    ConcurrentHashMap<String, Report> reports;

    public class Report {
        StringBuilder data;
        Long lastChunkUploaded;
        String Name;
        SamplingStatus status;

        private final Object lock = new Object();
        private boolean stop = false;
        private boolean isDone = false;

        public void finish() {
            this.stop = true;
        }

        public boolean isDone() {
            return this.isDone;
        }

        public class ReportUploader implements Runnable {
            Report report;

            public ReportUploader(Report report) {
                this.report = report;
            }

            @Override
            public void run() {
                this.report.upload();
            }
        }

        private Report(String name) {
            Name = name;
            synchronized (lock) {
                data = new StringBuilder();
            }
            lastChunkUploaded = System.currentTimeMillis();
            status = new SamplingStatus();
            stop = false;
            new Thread(new ReportUploader(this)).start();
        }

        public void append(String data) {
            synchronized (lock) {
                this.data.append(data);
            }
        }

        private void upload() {

            boolean lastTime = true;
            this.isDone = false;
            long timeFromLastChunk;
            while (!this.stop || lastTime) {
                timeFromLastChunk = System.currentTimeMillis() - this.lastChunkUploaded;
                String toSend = null;
                int length;
                synchronized (lock) {
                    length = this.data.length();
                    if (length > 0 && ((length > chunk_size) || (timeFromLastChunk > MaxUploadInterval))) {
                        toSend = this.data.toString();
                        this.data.setLength(0);
                        BmLog.debug(this.Name + " set data size:" + length);
                    }
                }

                if (toSend != null) {
                    try {
                        String reportName = URLEncoder.encode(this.Name, "UTF-8");
                        new DataUploader(reportName, toSend).run();

                        this.lastChunkUploaded = System.currentTimeMillis();
                        BmLog.debug("Chunk uploaded: " + this.Name);
                    } catch (Exception ex) {
                        BmLog.error(ex.getMessage());
                    }
                    this.status.NumberOfSamples++;
                    this.status.FileSize += length;
                } else {
                    if (!this.stop)
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            BmLog.error("Thread was interrupted during sleeping!", e);
                        }
                }
                lastTime = this.stop && !lastTime;
            }
            this.isDone = true;
        }
    }


    private Uploader() {
        chunk_size = JMeterUtils.getPropDefault("blazemeter.default_chunk_size", 100) * 1024;
        reports = new ConcurrentHashMap<String, Report>();
    }

    private boolean isRunning = false;

    public synchronized void Finalize() {
        if (isRunning) {
            DoFinalize();
        }
    }

    private void DoFinalize() {
        TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
        String userKey = BmTestManager.getInstance().getUserKey();
        String testId = testInfo.id;

        isRunning = false;
        for (Map.Entry<String, Report> item : reports.entrySet()) {
            item.getValue().finish();
        }

        boolean allDone;
        do {
            allDone = true;
            for (Map.Entry<String, Report> item : reports.entrySet()) {
                allDone = allDone && item.getValue().isDone();
            }
        }
        while (!allDone);

        BlazemeterApi.getInstance().stopTest(userKey, testId);
    }

    class DataUploader implements Runnable {

        private final String reportName;
        private final String data;
        private String dataType;

        public DataUploader(String reportName, String data) {
            this(reportName, data, "jtl");
        }

        public DataUploader(String reportName, String data, String dataType) {

            this.reportName = reportName;
            this.data = data;
            this.dataType = dataType;
        }

        @Override
        public void run() {
            TestInfo testInfo = BmTestManager.getInstance().getTestInfo();
            String userKey = BmTestManager.getInstance().getUserKey();
            String testId = testInfo.id;
            BmLog.debug(String.format("Data uploader sending report:%s , %d bytes  ", this.reportName, this.data.length()));
            BlazemeterApi.getInstance().dataUpload(userKey, testId, this.reportName, this.data, this.dataType);
        }
    }

    public synchronized void forceUpload(String filename, String data, String dataType) {

        new DataUploader(filename, data, dataType).run();
    }


    public synchronized void addSample(String filename, String data) {
        addSample(filename, data, 0);
    }

    public synchronized void addSample(String filename, String data, int count) {
        if (!isRunning)
            return;

        if (reports.containsKey(filename)) {
            reports.get(filename).append(data);

        } else {
            BmLog.error("Sampling not started yet! Count " + count);
            if (count < 10) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
                addSample(filename, data, count + 1);
            }
        }

    }

    private static final Object lock = new Object();

    public synchronized void samplingStarted(String filename) {
        BmLog.console("adding " + filename);
        synchronized (lock) {
            if (!reports.contains(filename))
                reports.put(filename, new Report(filename));
        }
        isRunning = true;
    }
}
