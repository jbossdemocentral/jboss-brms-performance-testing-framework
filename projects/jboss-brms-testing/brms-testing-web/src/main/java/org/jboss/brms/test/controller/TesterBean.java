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

    private static final String CUSTOMER_EVALUATION_PACKAGE = "org.jbpm.evaluation.customer";
    private static final String CUSTOMER_EVALUATION_NAME = "customereval";
    private static final String CUSTOMER_EVALUATION_ID = "org.jbpm.customer-evaluation";

    @Inject
    private GuvnorService guvnorService;

    @Inject
    private ProcessService processService;

    @Inject
    private MetricsService metricsService;

    private boolean startInParallel;
    private boolean runInIndividualKnowledgeSession;
    private int customerEvaluationInstances = 1;

    private Long currentMetricsId;
    private boolean pollEnabled = false;
    private long processMeanRuntime;
    private long processMinRuntime;
    private long processMaxRuntime;

    public List<ProcessIndicator> getAvailableProcesses() {
        final List<ProcessIndicator> processes = new ArrayList<ProcessStartParameters.ProcessIndicator>();

        if (guvnorService.isProcessAvailable(CUSTOMER_EVALUATION_PACKAGE, CUSTOMER_EVALUATION_NAME, CUSTOMER_EVALUATION_ID)) {
            processes.add(new ProcessIndicator(CUSTOMER_EVALUATION_PACKAGE, CUSTOMER_EVALUATION_ID, 0));
        }

        return processes;
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

    public int getCustomerEvaluationInstances() {
        return customerEvaluationInstances;
    }

    public void setCustomerEvaluationInstances(final int customerEvaluationInstances) {
        this.customerEvaluationInstances = customerEvaluationInstances;
    }

    public void startProcessInstances() {
        pollEnabled = true;
        processMeanRuntime = 0;
        processMinRuntime = 0;
        processMaxRuntime = 0;

        final ProcessStartParameters parameters = new ProcessStartParameters();

        parameters.addIndicator(new ProcessIndicator(CUSTOMER_EVALUATION_PACKAGE, CUSTOMER_EVALUATION_ID, getCustomerEvaluationInstances()));

        parameters.setRunInIndividualKnowledgeSession(isRunInIndividualKnowledgeSession());
        parameters.setStartInParallel(isStartInParallel());
        currentMetricsId = processService.runProcesses(parameters);
    }

    public boolean isPollEnabled() {
        return pollEnabled;
    }

    public int getNumberOfInstancesStarted() {
        return metricsService.getNumberOfInstancesStarted(new ProcessIdentifier(currentMetricsId, CUSTOMER_EVALUATION_PACKAGE, CUSTOMER_EVALUATION_ID));
    }

    public int getNumberOfInstancesEnded() {
        final int numOfInst = metricsService.getNumberOfInstancesEnded(new ProcessIdentifier(currentMetricsId, CUSTOMER_EVALUATION_PACKAGE,
                CUSTOMER_EVALUATION_ID));
        if (numOfInst == getCustomerEvaluationInstances()) {
            // Test finished.
            pollEnabled = false;

            // Get statistical metrics.
            final ProcessRuntimeMetrics rtm = metricsService.getMeanRunningTimeOfInstances(new ProcessIdentifier(currentMetricsId, CUSTOMER_EVALUATION_PACKAGE,
                    CUSTOMER_EVALUATION_ID));
            processMeanRuntime = rtm.getMeanRuntime();
            processMinRuntime = rtm.getMinRuntime();
            processMaxRuntime = rtm.getMaxRuntime();
        }
        return numOfInst;
    }

    public long getProcessMeanRuntime() {
        return processMeanRuntime;
    }

    public long getProcessMinRuntime() {
        return processMinRuntime;
    }

    public long getProcessMaxRuntime() {
        return processMaxRuntime;
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
}
