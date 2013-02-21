package org.jboss.brms.test.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.model.ProcessIdentifier;
import org.jboss.brms.test.service.GuvnorService;
import org.jboss.brms.test.service.MetricsService;
import org.jboss.brms.test.service.ProcessRuntimeMetrics;
import org.jboss.brms.test.service.ProcessService;
import org.jboss.brms.test.service.ProcessStartParameters;
import org.jboss.brms.test.service.ProcessStartParameters.ProcessIndicator;

@Named
@SessionScoped
public class TesterBean implements Serializable {
    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    @Inject
    private GuvnorService guvnorService;

    @Inject
    private ProcessService processService;

    @Inject
    private MetricsService metricsService;

    private ProcessIndicator[] processes;
    private boolean startInParallel;
    private boolean runInIndividualKnowledgeSession;

    private Long currentMetricsId;
    private boolean pollEnabled = false;
    private ProcessRuntimeMetrics metrics = new ProcessRuntimeMetrics();

    public ProcessIndicator[] getAvailableProcesses() {
        if ((processes == null) || (processes.length == 0)) {
            refreshProcesses();
        }
        return processes;
    }

    public void refreshProcesses() {
        processes = guvnorService.getProcessesFromGuvnor().toArray(new ProcessIndicator[0]);
    }

    public boolean isStartInParallel() {
        return startInParallel;
    }

    public void setStartInParallel(final boolean startInParallel) {
        this.startInParallel = startInParallel;
    }

    public boolean isRunInIndividualKnowledgeSession() {
        return runInIndividualKnowledgeSession;
    }

    public void setRunInIndividualKnowledgeSession(final boolean runInIndividualKnowledgeSession) {
        this.runInIndividualKnowledgeSession = runInIndividualKnowledgeSession;
    }

    public void startProcessInstances() {
        pollEnabled = true;
        metrics.reset();

        final ProcessStartParameters parameters = new ProcessStartParameters();
        for (final ProcessIndicator indicator : processes) {
            if (indicator.getNumberOfInstances() > 0) {
                parameters.addIndicator(indicator);
            }
        }

        parameters.setRunInIndividualKnowledgeSession(isRunInIndividualKnowledgeSession());
        parameters.setStartInParallel(isStartInParallel());
        currentMetricsId = processService.runProcesses(parameters);
    }

    public boolean isPollEnabled() {
        return pollEnabled;
    }

    public int getNumberOfInstancesStarted() {
        return metricsService.getNumberOfInstancesStarted(indicatorsToIdentifiers());
    }

    public int getNumberOfInstancesEnded() {
        final int numOfInstEnded = metricsService.getNumberOfInstancesEnded(indicatorsToIdentifiers());
        int expectedInstances = 0;
        for (final ProcessIndicator indicator : processes) {
            expectedInstances += indicator.getNumberOfInstances();
        }
        if (numOfInstEnded == expectedInstances) {
            // Test finished.
            pollEnabled = false;

            // Get statistical metrics.
            metrics = metricsService.getRuntimeMetrics(indicatorsToIdentifiers());
        }
        return numOfInstEnded;
    }

    public long getProcessMeanRuntime() {
        return metrics.getMeanRuntime();
    }

    public long getProcessMinRuntime() {
        return metrics.getMinRuntime();
    }

    public long getProcessMaxRuntime() {
        return metrics.getMaxRuntime();
    }

    public String getCurrentMetrics() {
        String output = null;
        if (!pollEnabled) {
            // Get raw metrics.
            final Metrics metrics = metricsService.findMetricsById(currentMetricsId);
            if (metrics != null) {
                output = metrics.printAll();
            }
        }
        return output;
    }

    private List<ProcessIdentifier> indicatorsToIdentifiers() {
        final List<ProcessIdentifier> identifiers = new ArrayList<ProcessIdentifier>();
        for (final ProcessIndicator indicator : processes) {
            if (indicator.getNumberOfInstances() > 0) {
                identifiers.add(new ProcessIdentifier(currentMetricsId, indicator.getPackageName(), indicator.getProcessId()));
            }
        }
        return identifiers;
    }
}
