package org.jboss.brms.test.service;

public class ProcessRuntimeMetrics {
    private long meanRuntime;
    private long minRuntime;
    private long maxRuntime;

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

}
