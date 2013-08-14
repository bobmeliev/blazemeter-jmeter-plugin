package com.blazemeter.jmeter.upload;

import org.json.JSONObject;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 8/13/13
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplesResultsUploader {
    private static LinkedBlockingQueue<JSONObject> resultsQueue = new LinkedBlockingQueue<JSONObject>(50);

    private SamplesResultsUploader() {
    }

    public static void start() {
        /*
        1.Start checking time;
        2.Start checking capacity;
        3.Send JSON to server;
         */
    }

    public static void stop() {
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
}
