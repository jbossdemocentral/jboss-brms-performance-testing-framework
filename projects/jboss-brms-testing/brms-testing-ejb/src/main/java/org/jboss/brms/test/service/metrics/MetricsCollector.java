package org.jboss.brms.test.service.metrics;

import java.util.Date;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;

public class MetricsCollector {
    private final Metrics metrics;

    private final ProcessEventListener processEventListener;

    public MetricsCollector(final StatefulKnowledgeSession ksession) {
        metrics = new Metrics();
        processEventListener = new ProcessEventListener(metrics);

        // Instrument the session.
        ksession.addEventListener(processEventListener);

        // Hard-code for now:
        metrics.setNumberOfMachines(Integer.valueOf(1));
        metrics.setLoadBalancingUsed(Boolean.FALSE);
        metrics.setProcessesStartedInParallel(Boolean.FALSE);
        metrics.setProcessesRunInIndividualKnowledgeSession(Boolean.FALSE);
    }

    public void startTest() {
        metrics.setStartingTime(new Date());
    }

    public void endTest() {
        metrics.setEndingTime(new Date());
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
