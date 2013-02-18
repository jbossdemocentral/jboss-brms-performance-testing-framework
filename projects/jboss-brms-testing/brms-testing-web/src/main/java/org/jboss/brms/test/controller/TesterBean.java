package org.jboss.brms.test.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.service.GuvnorService;
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

    private boolean startInParallel;
    private boolean runInIndividualKnowledgeSession;
    private int customerEvaluationInstances = 1;

    private String currentMetrics;

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
        final ProcessStartParameters parameters = new ProcessStartParameters();

        parameters.addIndicator(new ProcessIndicator(CUSTOMER_EVALUATION_PACKAGE, CUSTOMER_EVALUATION_ID, getCustomerEvaluationInstances()));

        parameters.setRunInIndividualKnowledgeSession(isRunInIndividualKnowledgeSession());
        parameters.setStartInParallel(isStartInParallel());
        final Metrics metrics = processService.runProcesses(parameters);

        // Temp output:
        currentMetrics = metrics.printAll();
    }

    public String getCurrentMetrics() {
        return currentMetrics;
    }
}
