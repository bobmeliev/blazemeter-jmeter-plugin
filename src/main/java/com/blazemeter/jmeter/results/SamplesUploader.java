package com.blazemeter.jmeter.results;

import org.apache.jmeter.samplers.SampleEvent;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 8/13/13
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplesUploader implements Runnable {
    private static SamplesUploader samplesUploader = null;
    private static final int batchSize = 50;
    private final SamplesQueue samplesQueue;

    private SamplesUploader(int batchSize) {
        this.samplesQueue = new SamplesQueue(batchSize);
    }

    public static SamplesUploader getInstance() {
        if (samplesUploader == null) {
            samplesUploader = new SamplesUploader(batchSize);
        }
        return samplesUploader;
    }

    public void start() {
        samplesUploader.start();
    }

    public void stop() {
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

    public void addSample(SampleEvent sampleEvent) {
       /*
       1.Add sample to samplesQueue
        */
    }

    public void addSamples(List<SampleEvent> sampleEvents) {
       /*
       1.Add List of samples to samplesQueue
        */
    }

    @Override
    public void run() {
        /*long delay = MAX_DELEAY;
        while (!isInterrupted()) {
            try {
                long begin = System.currentTimeMillis();
                List<String> batch = queue.take((int) delay);
                send(batch);
                long millisOfCurrentItration =  System.currentTimeMillis() - begin;
                delay = MAX_DELEAY - millisOfCurrentItration;
                if (delay < 0) {
                    // если отсылка заняла больше 30 секунд
                    delay = 0;
                }
            } catch (InterruptedException e) {
                super.interrupt();
            }
        }*/
    }
}
