package org.jboss.brms.test.service;

public class ProcessStartParameters {
    private String packageName;
    private String processId;
    private int numberOfInstances;
    private boolean startInParallel;
    private boolean runInIndividualKnowledgeSession;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(final String processId) {
        this.processId = processId;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(final int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
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
}
