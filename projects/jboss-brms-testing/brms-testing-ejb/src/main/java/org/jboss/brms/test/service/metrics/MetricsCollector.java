package org.jboss.brms.test.service.metrics;

import java.util.Date;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.ProcessStartParameters;

public class MetricsCollector {
    private final Metrics metrics;

    private final ProcessEventListener processEventListener;

    /**
     * Constructor for running a single process.
     * 
     * @param ksession
     *            The {@link StatefulKnowledgeSession} in which the instance is run.
     */
    public MetricsCollector(final StatefulKnowledgeSession ksession) {
        this((ProcessStartParameters) null);
        addSession(ksession);
    }

    /**
     * Constructor for running more complex scenarios
     * 
     * @param parameters
     *            The parameters for the scenarios.
     */
    public MetricsCollector(final ProcessStartParameters parameters) {
        metrics = new Metrics();
        processEventListener = new ProcessEventListener(metrics);

        // Hard-code for now:
        metrics.setNumberOfMachines(Integer.valueOf(1));
        metrics.setLoadBalancingUsed(Boolean.FALSE);

        if (parameters != null) {
            metrics.setProcessesStartedInParallel(parameters.isStartInParallel());
            metrics.setProcessesRunInIndividualKnowledgeSession(parameters.isRunInIndividualKnowledgeSession());
        } else {
            // Hard-code for single run:
            metrics.setProcessesStartedInParallel(Boolean.FALSE);
            metrics.setProcessesRunInIndividualKnowledgeSession(Boolean.FALSE);
        }
    }

    public void addSession(final StatefulKnowledgeSession ksession) {
        // Instrument the session.
        ksession.addEventListener(processEventListener);
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
