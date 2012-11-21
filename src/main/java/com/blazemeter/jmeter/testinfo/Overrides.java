package com.blazemeter.jmeter.testinfo;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 8/29/12
 * Time: 11:15
 */
public class Overrides {
    public int duration;
    public int iterations;
    public int rampup;
    public int threads;

    public Overrides(int duration, int iterations, int rampup, int threads) {
        this.duration = duration;
        this.iterations = iterations;
        this.rampup = rampup;
        this.threads = threads;
    }
}
