package com.blazemeter.jmeter.testexecutor;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/29/12
 * Time: 11:06
 */
public class SamplingStatus {
    public int NumberOfSamples;
    public int FileSize;
    public int PercentProgress;

    public SamplingStatus() {
        NumberOfSamples = 0;
        PercentProgress=0;
        FileSize=0;
    }

    @Override
    public String toString() {
        return String.format("SamplingStatus{NumberOfSamples=%d, FileSize=%d, PercentProgress=%d}", NumberOfSamples, FileSize, PercentProgress);
    }
}

