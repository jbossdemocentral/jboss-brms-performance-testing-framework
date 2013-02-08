package org.jboss.brms.test.service.metrics;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.MetricsService;
import org.jboss.brms.test.service.ProcessStartParameters;

public class MetricsCollector {
    private final MetricsService metricsService;
    private final Long metricsId;

    private final ProcessEventListener processEventListener;

    /**
     * Constructor for running a single process (e.g. in unit tests).
     * 
     * @param metricsFactory
     *            The service that governs all metrics related objects.
     * @param ksession
     *            The {@link StatefulKnowledgeSession} in which the instance is run.
     */
    public MetricsCollector(final MetricsService metricsService, final StatefulKnowledgeSession ksession) {
        this(metricsService, (ProcessStartParameters) null);
        addSession(ksession);
    }

    /**
     * Constructor for running more complex scenarios.
     * 
     * @param metricsService
     *            The service that governs all metrics related objects.
     * @param parameters
     *            The parameters for the scenarios.
     */
    public MetricsCollector(final MetricsService metricsService, final ProcessStartParameters parameters) {
        this.metricsService = metricsService;

        Metrics metrics = null;
        if (parameters != null) {
            // TODO: Hard-coded #machines and loadbalancing for now:
            metrics = metricsService.createMetrics(Integer.valueOf(1), Boolean.FALSE, parameters.isStartInParallel(),
                    parameters.isRunInIndividualKnowledgeSession());
        } else {
            // Hard-coded for single run:
            metrics = metricsService.createMetrics(Integer.valueOf(1), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
        }
        metricsId = metrics.getId();

        processEventListener = new ProcessEventListener(metricsService, metricsId);
    }

    public void addSession(final StatefulKnowledgeSession ksession) {
        // Instrument the session.
        ksession.addEventListener(processEventListener);
    }

    public void startTest() {
        metricsService.setTestStartTime(metricsId);
    }

    public void endTest() {
        metricsService.setTestEndTime(metricsId);
    }

    public Metrics getMetrics() {
        return metricsService.findMetricsById(metricsId);
    }
}
