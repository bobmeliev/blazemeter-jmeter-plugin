package com.blazemeter.jmeter.entities;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 8/29/12
 * Time: 11:15
 */

public class Overrides {
    private int duration;
    private int iterations;
    private int rampup;
    private int threads;

    public Overrides(int duration, int iterations, int rampup, int threads) {
        this.duration = duration;
        this.iterations = iterations;
        this.rampup = rampup;
        this.threads = threads;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getRampup() {
        return rampup;
    }

    public void setRampup(int rampup) {
        this.rampup = rampup;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}
