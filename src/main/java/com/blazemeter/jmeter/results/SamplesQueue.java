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
    private final ArrayList<JSONObject> messages;
    private final int batchSize;

    public SamplesQueue(int batchSize) {
        this.batchSize = batchSize;
        this.messages = new ArrayList<JSONObject>(batchSize);
    }

    public void put(JSONObject message) throws InterruptedException {
        lock.lock();
        try {
            messages.add(message);
            if (messages.size() >= batchSize) {
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
            if (messages.size() < batchSize) {
                lessThenBatchSizeCondition.await(maxWaitMillis, TimeUnit.MILLISECONDS);
            }
            if (messages.size() == 0) {
                return result;
            }
            int takedCount = messages.size() < batchSize ? messages.size() : batchSize;
            result.addAll(messages.subList(0, takedCount));
            messages.removeAll(result);
            return result;
        } finally {
            lock.unlock();
        }
    }

}