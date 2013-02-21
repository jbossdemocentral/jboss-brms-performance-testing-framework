package org.jboss.brms.test.service;

public class ProcessRuntimeMetrics {
    private long numberOfInstances;
    private long meanRuntime;
    private long minRuntime;
    private long maxRuntime;

    public long getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(final long numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public long getMeanRuntime() {
        return meanRuntime;
    }

    public void setMeanRuntime(final long meanRuntime) {
        this.meanRuntime = meanRuntime;
    }

    public long getMinRuntime() {
        return minRuntime;
    }

    public void setMinRuntime(final long minRuntime) {
        this.minRuntime = minRuntime;
    }

    public long getMaxRuntime() {
        return maxRuntime;
    }

    public void setMaxRuntime(final long maxRuntime) {
        this.maxRuntime = maxRuntime;
    }

    public void reset() {
        meanRuntime = 0;
        minRuntime = 0;
        maxRuntime = 0;
    }
}
