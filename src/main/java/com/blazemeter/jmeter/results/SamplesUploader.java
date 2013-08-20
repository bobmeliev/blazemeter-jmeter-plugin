package com.blazemeter.jmeter.results;

import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.BmLog;

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
    private static final int batchSize = 50;
    private static final SamplesQueue samplesQueue = new SamplesQueue(batchSize);
    private static final int MAX_DELAY = 30000;

    private SamplesUploader() {
    }


    public static void startUploading(String url) {
        final String callBackUrl = url;
        samplesUploaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long delay = MAX_DELAY;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        long begin = System.currentTimeMillis();
                        List<String> batch = samplesQueue.take((int) delay);
                        send(batch, callBackUrl);
                        long millisOfCurrentIteration = System.currentTimeMillis() - begin;
                        delay = MAX_DELAY - millisOfCurrentIteration;
                        if (delay < 0) {
                            // если отсылка заняла больше 30 секунд
                            delay = 0;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        task = scheduler.schedule(samplesUploaderThread, 0, TimeUnit.SECONDS);
        BmLog.console("Samples uploading is started");
    }

    public static void stop() {

        task.cancel(true);
        /*
        1.Stop checking time;
        2.Stop checking capacity;
        3. drainTo() all objects;
        4. Send them to server;
        5. Die.
         */

    }

    public static void addSample(String jsonString) {
        try {
            samplesQueue.put(jsonString);

        } catch (InterruptedException ie) {
            BmLog.error("Failed to add sample to uploader: Interruptedexception");
        }
    }

    private static void send(List<String> batch, String callBackUrl) {
        BmTestManager bmTestManager = BmTestManager.getInstance();
//        bmTestManager.logUpload();
        //send samples to server
    }
}
