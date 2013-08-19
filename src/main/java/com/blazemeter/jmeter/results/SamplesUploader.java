package com.blazemeter.jmeter.results;

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
    private static SamplesUploader samplesUploader = null;
    private static Thread samplesUploaderThread = null;
    private static final int batchSize = 50;
    private static final SamplesQueue samplesQueue = new SamplesQueue(batchSize);
    private static final int MAX_DELAY = 30000;

    private SamplesUploader() {
    }


    public static void startUploading() {
        samplesUploaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long delay = MAX_DELAY;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        long begin = System.currentTimeMillis();
                        List<String> batch = samplesQueue.take((int) delay);
                        send(batch);
                        long millisOfCurrentItration = System.currentTimeMillis() - begin;
                        delay = MAX_DELAY - millisOfCurrentItration;
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
        /*
        TODO
        1.QueueChecker;
        2.JTL2JSON converter;
        3.JSON2String converter
         */
    }

    public static void addSample(String jsonString) {
       /*
       1.Add sample to samplesQueue
        */
    }

    public static void addSamples(List<String> jsonStrings) {
       /*
       1.Add List of samples to samplesQueue
        */
    }

    private static void send(List<String> batch) {
        //send samples to server
    }
}
