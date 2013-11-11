package com.blazemeter.jmeter.results;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.Constants;
import com.blazemeter.jmeter.utils.BmLog;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 8/13/13
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplesUploader {
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> task;
    private static Thread samplesUploaderThread = null;
    private static StringBuilder callBackUrl = null;
    private static final int samplesSize = 50;
    private static final SamplesQueue samplesQueue = new SamplesQueue(samplesSize);
    private static final int MAX_DELAY = 30000;
    private static long millisOfCurrentIteration = 0;

    private SamplesUploader() {
    }


    public static void startUploading(String url) {
        if (url == null) {
            BmLog.error("Cannot start SamplesUploader: server did not send callBack URL");
        }
        callBackUrl = new StringBuilder(url);
        samplesUploaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    long delay = MAX_DELAY - millisOfCurrentIteration;
                    if (delay < 0) {
                        // if sending results to server took more than 30secs, set
                        millisOfCurrentIteration = 30000;
                    }
                    try {
                        List<JSONObject> samples = samplesQueue.take((int) delay);
                        if (samples.size() > 0) {
                            long begin = System.currentTimeMillis();
                            send(samples, callBackUrl.toString());
                            millisOfCurrentIteration = System.currentTimeMillis() - begin;
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        task = scheduler.schedule(samplesUploaderThread, 0, TimeUnit.SECONDS);
        BmLog.console("Samples uploading is started: " + Constants.CALLBACK_URL + "=" + callBackUrl);
    }

    public static void stop() {
        try {
            List<JSONObject> samples = samplesQueue.take(MAX_DELAY);
            if (samples.size() > 0) {
                send(samples, callBackUrl.toString());
            }
        } catch (InterruptedException ie) {
            BmLog.debug("Interrupted exception during finishing SamplesUploader: " + ie.getMessage());
        }
        if (task != null && !task.isDone()) {
            task.cancel(true);
            BmLog.console("Samples uploading is finished: " + Constants.CALLBACK_URL + "=" + callBackUrl);
        }
    }

    public static void addSample(JSONObject jsonObject) {
        try {
            samplesQueue.put(jsonObject);
        } catch (InterruptedException ie) {
            BmLog.error("Failed to add sample to uploader: Interruptedexception");
        }
    }

    private static void send(List<JSONObject> samples, String callBackUrl) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.samplesUpload(samples, callBackUrl);
    }
}
