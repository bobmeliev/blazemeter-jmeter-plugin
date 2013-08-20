package com.blazemeter.jmeter.results;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SamplesQueue {

    private final Lock lock = new ReentrantLock();
    private final Condition lessThenBatchSizeCondition = lock.newCondition();
    private final ArrayList<JSONObject> samples;
    private final int batchSize;

    public SamplesQueue(int batchSize) {
        this.batchSize = batchSize;
        this.samples = new ArrayList<JSONObject>(batchSize);
    }

    public void put(JSONObject sample) throws InterruptedException {
        lock.lock();
        try {
            samples.add(sample);
            if (samples.size() >= batchSize) {
                lessThenBatchSizeCondition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public List<JSONObject> take(int maxWaitMillis) throws InterruptedException {
        List<JSONObject> result = new ArrayList<JSONObject>();
        lock.lock();
        try {
            if (samples.size() < batchSize) {
                lessThenBatchSizeCondition.await(maxWaitMillis, TimeUnit.MILLISECONDS);
            }
            if (samples.size() == 0) {
                return result;
            }
            int takedCount = samples.size() < batchSize ? samples.size() : batchSize;
            result.addAll(samples.subList(0, takedCount));
            samples.removeAll(result);
            return result;
        } finally {
            lock.unlock();
        }
    }

}