package com.blazemeter.jmeter.entities;

/**
 * Created by dzmitrykashlach on 3/4/14.
 */
public class Plan {
    private String id;
    private String concurrency;
    private int engines;
    private boolean isMetered;
    private int threadsPerEngine;
    private int threadsPerMediumEngine;

    public Plan(String id, String concurrency, int engines, boolean isMetered, int threadsPerEngine, int threadsPerMediumEngine) {
        this.id = id;
        this.concurrency = concurrency;
        this.engines = engines;
        this.isMetered = isMetered;
        this.threadsPerEngine = threadsPerEngine;
        this.threadsPerMediumEngine = threadsPerMediumEngine;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    public int getEngines() {
        return engines;
    }

    public void setEngines(int engines) {
        this.engines = engines;
    }

    public boolean isMetered() {
        return isMetered;
    }

    public void setMetered(boolean isMetered) {
        this.isMetered = isMetered;
    }

    public int getThreadsPerEngine() {
        return threadsPerEngine;
    }

    public void setThreadsPerEngine(int threadsPerEngine) {
        this.threadsPerEngine = threadsPerEngine;
    }

    public int getThreadsPerMediumEngine() {
        return threadsPerMediumEngine;
    }

    public void setThreadsPerMediumEngine(int threadsPerMediumEngine) {
        this.threadsPerMediumEngine = threadsPerMediumEngine;
    }
}
